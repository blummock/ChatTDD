package com.blummock.chattdd.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
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
import com.blummock.chattdd.chat_feature.presentation.vm.state.MessageUiModel
import com.blummock.chattdd.chat_feature.presentation.vm.state.TextMessageModel
import com.blummock.chattdd.ui.theme.ChatTDDTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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

    @Before
    fun setup() {
        mapper = UiMapper(FakeTimeConverter())
        viewModel = ChatViewModel(
            uiMapper = mapper,
            savedStateHandle = SavedStateHandle(),
            observeMessagesUseCase = ObserveMessagesUseCase(fakeSendMessagesRepository),
            sendMessageUseCase = SendMessageUseCase(fakeSendMessagesRepository)
        )
        with(composeTestRule) {
            setContent {
                ChatTDDTheme {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }


    @Test
    fun `send text successfully`(): Unit = with(composeTestRule) {
        val sendButton = onNodeWithTag("sendButton")
        val textInput = onNodeWithTag("textInput")
        val errorMessage = onNodeWithTag("errorMessage")
        sendButton.assertIsNotEnabled()
        textInput.assertExists()
        textInput.performTextReplacement("some text")
        sendButton.assertIsEnabled()
        fakeSendMessagesRepository.result = ChatResult.Success(Unit)
        sendButton.performClick()
        textInput.assertTextEquals("")
        errorMessage.assertDoesNotExist()
    }

    @Test
    fun `send text with error`(): Unit = with(composeTestRule) {
        val sendButton = onNodeWithTag("sendButton")
        val textInput = onNodeWithTag("textInput")
        val errorMessage = onNodeWithTag("errorMessage")
        sendButton.assertIsNotEnabled()
        textInput.assertExists()
        val text = "some text"
        textInput.performTextReplacement(text)
        sendButton.assertIsEnabled()
        val error = DomainError.UnknownError
        fakeSendMessagesRepository.result = ChatResult.Error(error)
        sendButton.performClick()
        textInput.assertTextEquals(text)
        errorMessage.assertTextEquals(error.message)
    }

    @Test
    fun `loading list of messages success`(): Unit = with(composeTestRule) {
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
        onNodeWithTag("loadingList").assertExists()
        onNodeWithTag("messagesList").assertDoesNotExist()
        onNodeWithTag("errorList").assertDoesNotExist()
        onNodeWithTag("emptyList").assertDoesNotExist()
        fakeSendMessagesRepository.messages = flowOf(
            MessagesState.Data(messages)
        )
        messages.indices.forEach { index ->
            assertMessageAtPosition(index, mapper.toUi(messages[index]))
        }
    }

    @Test
    fun `loading list of messages fail`(): Unit = with(composeTestRule) {
        onNodeWithTag("loadingList").assertExists()
        onNodeWithTag("messagesList").assertDoesNotExist()
        onNodeWithTag("errorList").assertDoesNotExist()
        onNodeWithTag("emptyList").assertDoesNotExist()
        val error = DomainError.NoInternet
        fakeSendMessagesRepository.messages = flowOf(
            MessagesState.Error(error)
        )
        composeTestRule.onNode(
            hasTestTag("errorList") and
                    hasAnyAncestor(hasText(error.message))
        ).assertExists()
    }

    @Test
    fun `loading list of messages empty`(): Unit = with(composeTestRule) {
        onNodeWithTag("loadingList").assertExists()
        onNodeWithTag("messagesList").assertDoesNotExist()
        onNodeWithTag("errorList").assertDoesNotExist()
        onNodeWithTag("emptyList").assertDoesNotExist()
        fakeSendMessagesRepository.messages = flowOf(
            MessagesState.Data(emptyList())
        )
        onNodeWithTag("emptyList").assertExists()
    }

    private fun assertMessageAtPosition(position: Int, message: MessageUiModel) = with(composeTestRule) {
        onNodeWithTag("messagesList")
            .performScrollToNode(hasTestTag("Element at $position"))
            .assertIsDisplayed()
        when (message) {
            is TextMessageModel -> {
                composeTestRule.onNode(
                    hasTestTag("Element at $position") and
                            hasAnyAncestor(hasTestTag("whoos ${message.isMine}")) and
                            hasAnyDescendant(hasText(message.text)) and
                            hasAnyDescendant(hasTestTag("status ${message.status}")) and
                            hasAnyDescendant(hasText(message.time))
                ).assertExists()
            }
        }
    }
}

private class FakeTimeConverter : TimeConverter {
    override fun toHHmm(millis: Long) = millis.toString()
}

private class FakeMessagesRepository : MessagesRepository {

    lateinit var messages: Flow<MessagesState>
    lateinit var result: ChatResult<Unit>

    override fun observeMessages(): Flow<MessagesState> {
        return messages
    }

    override suspend fun postMessage(message: Message): ChatResult<Unit> {
        return result
    }
}