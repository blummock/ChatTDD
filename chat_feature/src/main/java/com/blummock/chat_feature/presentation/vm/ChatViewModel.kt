package com.blummock.chat_feature.presentation.vm

import androidx.lifecycle.SavedStateHandle
import com.blummock.chat_feature.domain.use_cases.ObserveMessagesUseCase
import com.blummock.chat_feature.domain.use_cases.SendMessageUseCase
import com.blummock.chat_feature.presentation.vm.state.ChatState
import com.blummock.chat_feature.presentation.vm.state.MessagesUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

internal class ChatViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val observeMessagesUseCase: ObserveMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
) {
    private val _state = MutableStateFlow(
        ChatState(
            messagesUiState = MessagesUiState.Loading,
            messageInput = "",
            sendButtonEnabled = false,
        )
    )
    val state = _state.asStateFlow()

    private val _effect = Channel<ChatEffect>()
    val effect = _effect.receiveAsFlow()

    fun setMessageInput(text: String) {
        _state.update {
            it.copy(messageInput = text)
        }
    }

    fun sendMessage() {

    }
}