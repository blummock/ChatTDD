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
    private val composeTestRule = createComposeRule()
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
    fun `send text successfully`() {
        chatScreenPage
            .assertSendButtonDisabled()
            .assertTextInputExists()
            .typeTextInput("some text")
            .assertSendButtonEnabled()
        fakeSendMessagesRepository.result = ChatResult.Success(Unit)
        chatScreenPage
            .clickSendButton()
            .assertTextInputEquals("")
            .assertErrorMessageNotExists()
    }

    @Test
    fun `send text with error`() {
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
            .assertErrorWithMessage(error.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loading list of messages success`() {
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
            .assertErrorMessageNotExists()
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
    fun `update list of messages after sending`() {
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
            .scrollListToPosition(0)
            .assertElementAtNotDisplayed(items - 1)
            .typeTextInput("some text")

        fakeSendMessagesRepository.result = ChatResult.Success(Unit)
        chatScreenPage.clickSendButton()
        runTest {
            fakeSendMessagesRepository.messages.emit(MessagesState.Data(messages))
        }
        chatScreenPage.assertElementAtDisplayed(items - 1)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loading list of messages fail`() {
        chatScreenPage
            .assertLoadingExists()
            .assertMessagesListNotExists()
            .assertErrorMessageNotExists()
            .assertEmptyListNotExists()
        val error = DomainError.NoInternet
        runTest {
            fakeSendMessagesRepository.messages.emit(MessagesState.Error(error))
        }
        chatScreenPage
            .assertErrorWithMessage(error.message)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loading list of messages empty`() {
        chatScreenPage
            .assertLoadingExists()
            .assertMessagesListNotExists()
            .assertErrorMessageNotExists()
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