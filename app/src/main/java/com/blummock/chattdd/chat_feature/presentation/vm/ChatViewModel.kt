package com.blummock.chattdd.chat_feature.presentation.vm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blummock.chattdd.chat_feature.domain.entity.ChatResult
import com.blummock.chattdd.chat_feature.domain.entity.MessagesState
import com.blummock.chattdd.chat_feature.domain.use_cases.ObserveMessagesUseCase
import com.blummock.chattdd.chat_feature.domain.use_cases.SendMessageUseCase
import com.blummock.chattdd.chat_feature.presentation.vm.state.ChatState
import com.blummock.chattdd.chat_feature.presentation.vm.state.MessagesUiState
import com.blummock.chattdd.chat_feature.presentation.vm.state.UiMapper
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ChatViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val observeMessagesUseCase: ObserveMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val uiMapper: UiMapper,
) : ViewModel() {
    private val _state = MutableStateFlow(
        ChatState(
            messagesUiState = MessagesUiState.Loading,
            messageInput = savedStateHandle.get<String>(INPUT_KEY) ?: "",
            sendButtonEnabled = !savedStateHandle.get<String>(INPUT_KEY).isNullOrBlank(),
        )
    )
    val state = _state.asStateFlow()

    private val _effect = Channel<ChatEffect>()
    val effect = _effect.receiveAsFlow()
    private var sendingJob: Job? = null

    init {
        viewModelScope.launch {
            observeMessagesUseCase()
                .collect { messageState ->
                    val messages = messageState.messages.map { uiMapper.toUi(it) }
                    _state.update {
                        it.copy(
                            messagesUiState = if (messages.isEmpty()) {
                                MessagesUiState.Empty
                            } else {
                                MessagesUiState.Data(messages)
                            }
                        )
                    }
                    if (messageState is MessagesState.Error) {
                        _effect.send(ChatEffect.ErrorEffect(messageState.reason.message))
                    }
                }
        }
    }

    fun setMessageInput(text: String) {
        _state.update {
            it.copy(messageInput = text, sendButtonEnabled = !text.isBlank())
        }
        savedStateHandle[INPUT_KEY] = text
    }

    fun sendMessage() {
        if (sendingJob?.isActive == true) return
        sendingJob = viewModelScope.launch {
            when (val result = sendMessageUseCase(_state.value.messageInput)) {
                is ChatResult.Error -> _effect.send(ChatEffect.ErrorEffect(result.error.message))
                is ChatResult.Success -> {
                    _effect.send(ChatEffect.ScrollToBottom)
                    _state.update { it.copy(messageInput = "", sendButtonEnabled = false) }
                    savedStateHandle[INPUT_KEY] = ""
                }
            }
        }
    }

    private companion object {
        const val INPUT_KEY = "input_key"
    }
}