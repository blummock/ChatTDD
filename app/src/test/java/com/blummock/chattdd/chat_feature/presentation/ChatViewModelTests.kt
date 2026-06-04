package com.blummock.chattdd.chat_feature.presentation

import androidx.lifecycle.SavedStateHandle
import com.blummock.chattdd.chat_feature.domain.entity.ChatResult
import com.blummock.chattdd.chat_feature.domain.entity.DomainError
import com.blummock.chattdd.chat_feature.domain.entity.Message
import com.blummock.chattdd.chat_feature.domain.entity.MessagesState
import com.blummock.chattdd.chat_feature.domain.entity.TextMessage
import com.blummock.chattdd.chat_feature.domain.repositories.MessagesRepository
import com.blummock.chattdd.chat_feature.domain.use_cases.ObserveMessagesUseCase
import com.blummock.chattdd.chat_feature.domain.use_cases.SendMessageUseCase
import com.blummock.chattdd.chat_feature.presentation.vm.ChatEffect
import com.blummock.chattdd.chat_feature.presentation.vm.ChatViewModel
import com.blummock.chattdd.chat_feature.presentation.vm.state.MessageStatusUi
import com.blummock.chattdd.chat_feature.presentation.vm.state.MessagesUiState
import com.blummock.chattdd.chat_feature.presentation.vm.state.TextMessageModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ChatViewModelTests {

    private lateinit var viewModel: ChatViewModel
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var messagesRepository: FakeMessagesRepository
    private lateinit var observeMessagesUseCase: ObserveMessagesUseCase
    private lateinit var sendMessageUseCase: SendMessageUseCase

    @Before
    fun setup() {
        savedStateHandle = SavedStateHandle()
        messagesRepository = FakeMessagesRepository()
        observeMessagesUseCase = ObserveMessagesUseCase(messagesRepository)
        sendMessageUseCase = SendMessageUseCase(messagesRepository)
        viewModel = ChatViewModel(
            savedStateHandle = savedStateHandle,
            observeMessagesUseCase = observeMessagesUseCase,
            sendMessageUseCase = sendMessageUseCase,
        )
    }

    @Test
    fun `loading messages not empty`() = runTest {
        val listOfMessages = listOf(
            TextMessage(
                id = "delectus",
                chatId = "mediocrem",
                senderId = "nascetur",
                timestamp = 1780457123,
                status = Message.MessageStatus.DELIVERED,
                text = "ignota"
            ),
            TextMessage(
                id = "habitasse",
                chatId = "dignissim",
                senderId = "meliore",
                timestamp = 1780457123,
                status = Message.MessageStatus.SENDING,
                text = "tale"
            )
        )
        val time = listOf("03:25", "03:42").iterator()
        val status = listOf(MessageStatusUi.DELIVERED, MessageStatusUi.SENDING).iterator()
        assertEquals("initial", MessagesUiState.Loading, viewModel.state.value.messagesUiState)
        messagesRepository.messagesState.emit(MessagesState.Data(listOfMessages))
        val expected = listOfMessages.map {
            TextMessageModel(
                id = it.id,
                timestamp = time.next(),
                status = status.next(),
                text = it.text
            )
        }
        assertEquals(MessagesUiState.Data(expected), viewModel.state.value.messagesUiState)
    }

    @Test
    fun `load messages empty`() = runTest {
        assertEquals("initial", MessagesUiState.Loading, viewModel.state.value.messagesUiState)
        messagesRepository.messagesState.emit(MessagesState.Data(emptyList()))
        assertEquals(MessagesUiState.Empty, viewModel.state.value.messagesUiState)
    }

    @Test
    fun `load messages error`() = runTest {
        assertEquals("initial", MessagesUiState.Loading, viewModel.state.value.messagesUiState)
        messagesRepository.messagesState.emit(MessagesState.Error(DomainError.NoInternet))
        assertEquals(MessagesUiState.Empty, viewModel.state.value.messagesUiState)
        assertEquals(ChatEffect.ErrorEffect(DomainError.NoInternet.message), viewModel.effect.first())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `send message success`() = runTest {
        val testText = "test text"
        assertFalse(viewModel.state.value.sendButtonEnabled)
        viewModel.setMessageInput(testText)
        assertEquals(testText, viewModel.state.value.messageInput)
        assertTrue(viewModel.state.value.sendButtonEnabled)
        messagesRepository.postMessageResult = ChatResult.Success(Unit)
        viewModel.sendMessage()
        assertEquals(ChatEffect.ScrollToBottom, viewModel.effect.first())
        assertEquals("", viewModel.state.value.messageInput)
    }

    @Test
    fun `send message error`() = runTest {
        val testText = "test text"
        assertFalse(viewModel.state.value.sendButtonEnabled)
        viewModel.setMessageInput(testText)
        messagesRepository.postMessageResult = ChatResult.Error(DomainError.NoInternet)
        viewModel.sendMessage()
        assertEquals(ChatEffect.ErrorEffect(DomainError.NoInternet.message), viewModel.effect.first())
        assertEquals(testText, viewModel.state.value.messageInput)
    }

    @Test
    fun `input is disabled`() = runTest {
        assertFalse(viewModel.state.value.sendButtonEnabled)
        viewModel.setMessageInput("some text")
        assertTrue(viewModel.state.value.sendButtonEnabled)
        viewModel.setMessageInput("")
        assertFalse(viewModel.state.value.sendButtonEnabled)
    }

    @Test
    fun `recreating ViewModel`() = runTest {
        val testText = "test text"
        viewModel.setMessageInput(testText)
        assertTrue(viewModel.state.value.sendButtonEnabled)
        viewModel = ChatViewModel(
            savedStateHandle = savedStateHandle,
            observeMessagesUseCase = observeMessagesUseCase,
            sendMessageUseCase = sendMessageUseCase,
        )
        assertTrue(viewModel.state.value.sendButtonEnabled)
        assertEquals(testText, viewModel.state.value.messageInput)
    }
}

class FakeMessagesRepository : MessagesRepository {

    val messagesState = MutableSharedFlow<MessagesState>()
    lateinit var postMessageResult: ChatResult<Unit>

    override fun observeMessages(): Flow<MessagesState> {
        return messagesState
    }

    override suspend fun postMessage(message: Message): ChatResult<Unit> {
        return postMessageResult
    }
}