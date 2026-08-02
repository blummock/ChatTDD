package com.blummock.chattdd.chat_feature.presentation.vm

import androidx.lifecycle.SavedStateHandle
import com.blummock.chattdd.chat_feature.core.TimeConverter
import com.blummock.chattdd.chat_feature.domain.entity.ChatResult
import com.blummock.chattdd.chat_feature.domain.entity.DomainError
import com.blummock.chattdd.chat_feature.domain.entity.Message
import com.blummock.chattdd.chat_feature.domain.entity.MessagesState
import com.blummock.chattdd.chat_feature.domain.entity.TextMessage
import com.blummock.chattdd.chat_feature.domain.repositories.MessagesRepository
import com.blummock.chattdd.chat_feature.domain.use_cases.ObserveMessagesUseCase
import com.blummock.chattdd.chat_feature.domain.use_cases.SendMessageUseCase
import com.blummock.chattdd.chat_feature.presentation.vm.state.MessagesUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ChatViewModelTests {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ChatViewModel
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var messagesRepository: FakeMessagesRepository
    private lateinit var observeMessagesUseCase: ObserveMessagesUseCase
    private lateinit var sendMessageUseCase: SendMessageUseCase
    private lateinit var mapper: UiMapper

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        savedStateHandle = SavedStateHandle()
        messagesRepository = FakeMessagesRepository()
        observeMessagesUseCase = ObserveMessagesUseCase(messagesRepository)
        sendMessageUseCase = SendMessageUseCase(messagesRepository)
        mapper = UiMapper(FakeTimeConverter())
        viewModel = ChatViewModel(
            uiMapper = mapper,
            savedStateHandle = savedStateHandle,
            observeMessagesUseCase = observeMessagesUseCase,
            sendMessageUseCase = sendMessageUseCase,
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `When viewModel inits then state is loading and when loading is success then get messages`() = runTest {
        val listOfMessages = listOf(
            TextMessage(
                id = "delectus",
                chatId = "mediocrem",
                senderId = "nascetur",
                timestamp = 1780457123,
                status = Message.MessageStatus.DELIVERED,
                isMine = false,
                text = "ignota"
            ),
            TextMessage(
                id = "habitasse",
                chatId = "dignissim",
                senderId = "meliore",
                timestamp = 1780457123,
                status = Message.MessageStatus.SENDING,
                isMine = true,
                text = "tale"
            )
        )
        assertEquals("initial", MessagesUiState.Loading, viewModel.state.value.messagesUiState)
        messagesRepository.messagesState.emit(MessagesState.Data(listOfMessages))
        val expected = listOfMessages.map { mapper.toUi(it) }
        assertEquals(MessagesUiState.Data(expected), viewModel.state.value.messagesUiState)
    }

    @Test
    fun `When messages list updates then the viewModels has updated list`() = runTest {
        val listOfMessages = listOf(
            TextMessage(
                id = "delectus",
                chatId = "mediocrem",
                senderId = "nascetur",
                timestamp = 1780457123,
                status = Message.MessageStatus.DELIVERED,
                isMine = false,
                text = "ignota"
            ),
            TextMessage(
                id = "habitasse",
                chatId = "dignissim",
                senderId = "meliore",
                timestamp = 1780457123,
                status = Message.MessageStatus.SENDING,
                isMine = true,
                text = "tale"
            )
        )
        assertEquals("initial", MessagesUiState.Loading, viewModel.state.value.messagesUiState)
        messagesRepository.messagesState.emit(MessagesState.Data(listOfMessages))
        var expected = listOfMessages.map { mapper.toUi(it) }
        assertEquals(MessagesUiState.Data(expected), viewModel.state.value.messagesUiState)
        val newListOfMessages = listOfMessages + TextMessage(
            id = "habiwedse1",
            chatId = "digsdnissim",
            senderId = "meliore",
            timestamp = 1780457125,
            status = Message.MessageStatus.SENDING,
            isMine = true,
            text = "new text"
        )
        messagesRepository.messagesState.emit(MessagesState.Data(newListOfMessages))
        expected = newListOfMessages.map { mapper.toUi(it) }
        assertEquals(MessagesUiState.Data(expected), viewModel.state.value.messagesUiState)
    }

    @Test
    fun `When viewModel get empty messages then the state becomes empty`() = runTest {
        assertEquals("initial", MessagesUiState.Loading, viewModel.state.value.messagesUiState)
        messagesRepository.messagesState.emit(MessagesState.Data(emptyList()))
        assertEquals(MessagesUiState.Empty, viewModel.state.value.messagesUiState)
    }

    @Test
    fun `When viewModel get load messages with error then the state becomes empty and sends event message`() = runTest {
        assertEquals("initial", MessagesUiState.Loading, viewModel.state.value.messagesUiState)
        messagesRepository.messagesState.emit(MessagesState.Error(DomainError.NoInternet))
        assertEquals(MessagesUiState.Empty, viewModel.state.value.messagesUiState)
        assertEquals(ChatEffect.ErrorEffect(DomainError.NoInternet.message), viewModel.effect.first())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `When viewModel sends message with success then message input clears and send cta disabled`() = runTest {
        val testText = "test text"
        assertFalse(viewModel.state.value.sendButtonEnabled)
        viewModel.setMessageInput(testText)
        assertEquals(testText, viewModel.state.value.messageInput)
        assertTrue(viewModel.state.value.sendButtonEnabled)
        messagesRepository.postMessageResult = ChatResult.Success(Unit)
        viewModel.sendMessage()
        advanceUntilIdle()
        assertEquals("", viewModel.state.value.messageInput)
        assertFalse(viewModel.state.value.sendButtonEnabled)
        assertEquals(1, messagesRepository.postMessageCalls)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `When calls send message multiple times then send happens once`() = runTest {
        val testText = "test text"
        assertFalse(viewModel.state.value.sendButtonEnabled)
        viewModel.setMessageInput(testText)
        assertEquals(testText, viewModel.state.value.messageInput)
        assertTrue(viewModel.state.value.sendButtonEnabled)
        messagesRepository.delay = 500L
        messagesRepository.postMessageResult = ChatResult.Success(Unit)
        viewModel.sendMessage()
        viewModel.sendMessage()
        advanceUntilIdle()
        assertEquals(1, messagesRepository.postMessageCalls)
    }

    @Test
    fun `When viewModel sends message with error then message input is kept with send button state`() = runTest {
        val testText = "test text"
        assertFalse(viewModel.state.value.sendButtonEnabled)
        viewModel.setMessageInput(testText)
        messagesRepository.postMessageResult = ChatResult.Error(DomainError.NoInternet)
        viewModel.sendMessage()
        assertEquals(ChatEffect.ErrorEffect(DomainError.NoInternet.message), viewModel.effect.first())
        assertEquals(testText, viewModel.state.value.messageInput)
    }

    @Test
    fun `When message input in viewModel is cleared then the send button state changes relatively`() = runTest {
        assertFalse(viewModel.state.value.sendButtonEnabled)
        viewModel.setMessageInput("some text")
        assertTrue(viewModel.state.value.sendButtonEnabled)
        viewModel.setMessageInput("")
        assertFalse(viewModel.state.value.sendButtonEnabled)
    }

    @Test
    fun `When ViewModel recreates with savedStateHandle then the input with send button state restores`() = runTest {
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

private class FakeTimeConverter : TimeConverter {
    override fun toHHmm(millis: Long) = millis.toString()
}

private class FakeMessagesRepository : MessagesRepository {

    val messagesState = MutableSharedFlow<MessagesState>()
    var delay = 0L
    var postMessageCalls = 0
        private set
    lateinit var postMessageResult: ChatResult<Unit>

    override fun observeMessages(): Flow<MessagesState> {
        return messagesState
    }

    override suspend fun postMessage(message: Message): ChatResult<Unit> {
        delay(delay)
        postMessageCalls++
        return postMessageResult
    }
}