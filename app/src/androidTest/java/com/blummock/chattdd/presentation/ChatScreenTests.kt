package com.blummock.chattdd.presentation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.blummock.chattdd.chat_feature.core.TimeConverter
import com.blummock.chattdd.chat_feature.domain.entity.ChatResult
import com.blummock.chattdd.chat_feature.domain.entity.DomainError
import com.blummock.chattdd.chat_feature.domain.entity.Message
import com.blummock.chattdd.chat_feature.domain.entity.MessagesState
import com.blummock.chattdd.chat_feature.domain.entity.TextMessage
import com.blummock.chattdd.chat_feature.domain.repositories.MessagesRepository
import com.blummock.chattdd.chat_feature.domain.use_cases.ObserveMessagesUseCase
import com.blummock.chattdd.chat_feature.domain.use_cases.SendMessageUseCase
import com.blummock.chattdd.ui.theme.ChatTDDTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatScreenTests {

    @get:Rule
    val composeTestRule = createComposeRule()
    private lateinit var viewModel: ChatViewModel
    private lateinit var mapper: UiMapper
    private lateinit var fakeSendMessagesRepository: FakeMessagesRepository
    private lateinit var chatScreenPage: ChatScreenPage

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        mapper = UiMapper(FakeTimeConverter())
        fakeSendMessagesRepository = FakeMessagesRepository()
        viewModel = ChatViewModel(
            uiMapper = mapper,
            savedStateHandle = SavedStateHandle(),
            observeMessagesUseCase = ObserveMessagesUseCase(fakeSendMessagesRepository),
            sendMessageUseCase = SendMessageUseCase(fakeSendMessagesRepository)
        )
        with(composeTestRule) {
            setContent {
                ChatTDDTheme {
                    ChatScreen(viewModel = viewModel)
                }
            }
        }
        chatScreenPage = ChatScreenPage(composeTestRule)
    }

    @Test
    fun `when text is sent successfully then input clears and no error message appears`() {
        chatScreenPage
            .assertSendButtonDisabled()
            .assertTextInputExists()
            .typeTextInput("some text")
            .assertSendButtonEnabled()
        fakeSendMessagesRepository.result = ChatResult.Success(Unit)
        chatScreenPage
            .clickSendButton()
            .assertTextInputEquals("")
            .assertToastNotExists()
    }

    @Test
    fun `when text is sent with error then input remains and the error message appears`() {
        val text = "some text"
        chatScreenPage
            .assertSendButtonDisabled()
            .assertTextInputExists()
            .typeTextInput(text)
            .assertSendButtonEnabled()
        val error = DomainError.UnknownError
        fakeSendMessagesRepository.result = ChatResult.Error(error)

        chatScreenPage
            .clickSendButton()
            .assertTextInputEquals(text)
            .assertToastMessage(error.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `when loading starts then progress appears and when loading is success then messages are shown`() {
        val messages = listOf(
            TextMessage(
                id = "0",
                chatId = "sem",
                senderId = "persius",
                timestamp = 8115,
                status = Message.MessageStatus.DELIVERED,
                isMine = false,
                text = "appetere"
            ),
            TextMessage(
                id = "1",
                chatId = "rutrum",
                senderId = "risus",
                timestamp = 4332,
                status = Message.MessageStatus.ERROR,
                isMine = true,
                text = "erroribus"
            ),
            TextMessage(
                id = "3",
                chatId = "mattis",
                senderId = "proin",
                timestamp = 2816,
                status = Message.MessageStatus.SENDING,
                isMine = true,
                text = "pellentesque"
            ),
            TextMessage(
                id = "4",
                chatId = "necessitatibus",
                senderId = "parturient",
                timestamp = 9581,
                status = Message.MessageStatus.SENDING,
                isMine = true,
                text = "vitae"
            )
        )

        chatScreenPage
            .assertLoadingExists()
            .assertMessagesListNotExists()
            .assertToastNotExists()
            .assertEmptyListNotExists()
        runTest {
            fakeSendMessagesRepository.messages.emit(MessagesState.Data(messages))
        }
        messages.indices.forEach { index ->
            chatScreenPage.assertMessageAtPosition(index, mapper.toUi(messages[index]))
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, ExperimentalTestApi::class)
    @Test
    fun `when messages are updated and a recently message's visible then list scrolls to the recently message`() {
        val items = 100
        val messages = List(items) { index ->
            TextMessage(
                id = "$index",
                chatId = "sem",
                senderId = "persius",
                timestamp = 8115,
                status = Message.MessageStatus.DELIVERED,
                isMine = false,
                text = "appetere $index"
            )
        }
        runTest {
            fakeSendMessagesRepository.messages.emit(MessagesState.Data(messages))
        }
        chatScreenPage
            .assertElementAtDisplayed(items - 1)
        val newItemIndex = 100
        val newMessages = messages + TextMessage(
            id = "$newItemIndex",
            chatId = "sda",
            senderId = "persius",
            timestamp = 8116,
            status = Message.MessageStatus.DELIVERED,
            isMine = false,
            text = "new message"
        )
        runTest {
            fakeSendMessagesRepository.messages.emit(MessagesState.Data(newMessages))
        }
        chatScreenPage.assertElementAtDisplayed(newItemIndex)
    }

    @OptIn(ExperimentalCoroutinesApi::class, ExperimentalTestApi::class)
    @Test
    fun `when messages are updated and a recently message is invisible then the list remains at the same position`() {
        val items = 100
        val messages = List(items) { index ->
            TextMessage(
                id = "$index",
                chatId = "sem",
                senderId = "persius",
                timestamp = 8115,
                status = Message.MessageStatus.DELIVERED,
                isMine = false,
                text = "appetere $index"
            )
        }
        runTest {
            fakeSendMessagesRepository.messages.emit(MessagesState.Data(messages))
        }
        val currentPosition = 50
        chatScreenPage
            .assertElementAtDisplayed(items - 1)
            .scrollListToPosition(currentPosition)
        val newItemIndex = 100
        val newMessages = messages + TextMessage(
            id = "$newItemIndex",
            chatId = "sda",
            senderId = "persius",
            timestamp = 8116,
            status = Message.MessageStatus.DELIVERED,
            isMine = false,
            text = "new message"
        )
        runTest {
            fakeSendMessagesRepository.messages.emit(MessagesState.Data(newMessages))
        }
        chatScreenPage
            .assertElementAtNotDisplayed(newItemIndex)
            .assertElementAtDisplayed(currentPosition)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `when messages loading fails then the error message appears`() {
        chatScreenPage
            .assertLoadingExists()
            .assertMessagesListNotExists()
            .assertToastNotExists()
            .assertEmptyListNotExists()
        val error = DomainError.NoInternet
        runTest {
            fakeSendMessagesRepository.messages.emit(MessagesState.Error(error))
        }
        chatScreenPage
            .assertToastMessage(error.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `when after loading messages are empty then the empty-list screen appears`() {
        chatScreenPage
            .assertLoadingExists()
            .assertMessagesListNotExists()
            .assertToastNotExists()
            .assertEmptyListNotExists()
        runTest {
            fakeSendMessagesRepository.messages.emit(MessagesState.Data(emptyList()))
        }
        chatScreenPage.assertEmptyListExists()
    }
}

private class FakeTimeConverter : TimeConverter {
    override fun toHHmm(millis: Long) = millis.toString()
}

private class FakeMessagesRepository : MessagesRepository {

    val messages = MutableSharedFlow<MessagesState>()
    lateinit var result: ChatResult<Unit>

    override fun observeMessages(): Flow<MessagesState> {
        return messages
    }

    override suspend fun postMessage(message: Message): ChatResult<Unit> {
        return result
    }
}