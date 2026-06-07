package com.blummock.chattdd.presentation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotDisplayed
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatScreenTests {

    @get:Rule
    val composeTestRule = createComposeRule()
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ChatViewModel
    private lateinit var mapper: UiMapper
    private lateinit var fakeSendMessagesRepository: FakeMessagesRepository

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
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
                    ChatScreen(viewModel = viewModel)
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `send text successfully`(): Unit = with(composeTestRule) {
        val sendButton = composeTestRule.onNodeWithTag("sendButton")
        val textInput = composeTestRule.onNodeWithTag("textInput")
        sendButton.assertIsNotEnabled()
        textInput.assertExists()
        textInput.performTextReplacement("some text")
        sendButton.assertIsEnabled()
        fakeSendMessagesRepository.result = ChatResult.Success(Unit)
        sendButton.performClick()
        textInput.assertTextEquals("")
        composeTestRule.onNodeWithTag("errorMessage").assertDoesNotExist()
    }

    @Test
    fun `send text with error`(): Unit = with(composeTestRule) {
        val sendButton = onNodeWithTag("sendButton")
        val textInput = onNodeWithTag("textInput")
        sendButton.assertIsNotEnabled()
        textInput.assertExists()
        val text = "some text"
        textInput.performTextReplacement(text)
        sendButton.assertIsEnabled()
        val error = DomainError.UnknownError
        fakeSendMessagesRepository.result = ChatResult.Error(error)
        sendButton.performClick()
        textInput.assertTextEquals(text)
        composeTestRule.onNode(
            hasTestTag("errorMessage") and
                    hasAnyAncestor(hasText(error.message))
        ).assertExists()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loading list of messages success`(): Unit = runTest {
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
        composeTestRule.onNodeWithTag("loadingList").assertExists()
        composeTestRule.onNodeWithTag("messagesList").assertDoesNotExist()
        composeTestRule.onNodeWithTag("errorMessage").assertDoesNotExist()
        composeTestRule.onNodeWithTag("emptyList").assertDoesNotExist()
        launch {
            fakeSendMessagesRepository.messages.emit(MessagesState.Data(messages))
        }
        advanceUntilIdle()
        messages.indices.forEach { index ->
            assertMessageAtPosition(index, mapper.toUi(messages[index]))
        }
        composeTestRule.onNodeWithTag("errorMessage").assertDoesNotExist()
    }

    @OptIn(ExperimentalCoroutinesApi::class, ExperimentalTestApi::class)
    @Test
    fun `update list of messages after sending`(): Unit = with(composeTestRule) {
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
        onNodeWithTag("Element at ${items - 1}").assertIsNotDisplayed()
        onNodeWithTag("textInput").performTextReplacement("some text")
        fakeSendMessagesRepository.result = ChatResult.Success(Unit)
        onNodeWithTag("sendButton").performClick()
        runTest {
            fakeSendMessagesRepository.messages.emit(MessagesState.Data(messages))
        }
        onNodeWithTag("Element at ${items - 1}").assertIsDisplayed()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loading list of messages fail`(): Unit = runTest {
        composeTestRule.onNodeWithTag("loadingList").assertExists()
        composeTestRule.onNodeWithTag("messagesList").assertDoesNotExist()
        composeTestRule.onNodeWithTag("errorMessage").assertDoesNotExist()
        composeTestRule.onNodeWithTag("emptyList").assertDoesNotExist()
        val error = DomainError.NoInternet
        launch {
            fakeSendMessagesRepository.messages.emit(MessagesState.Error(error))
        }
        advanceUntilIdle()
        composeTestRule.onNode(
            hasTestTag("errorMessage") and
                    hasAnyAncestor(hasText(error.message))
        ).assertExists()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loading list of messages empty`(): Unit = runTest {
        composeTestRule.onNodeWithTag("loadingList").assertExists()
        composeTestRule.onNodeWithTag("messagesList").assertDoesNotExist()
        composeTestRule.onNodeWithTag("errorMessage").assertDoesNotExist()
        composeTestRule.onNodeWithTag("emptyList").assertDoesNotExist()
        launch {
            fakeSendMessagesRepository.messages.emit(MessagesState.Data(emptyList()))
        }
        advanceUntilIdle()
        composeTestRule.onNodeWithTag("emptyList").assertExists()
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

    val messages = MutableSharedFlow<MessagesState>()
    lateinit var result: ChatResult<Unit>

    override fun observeMessages(): Flow<MessagesState> {
        return messages
    }

    override suspend fun postMessage(message: Message): ChatResult<Unit> {
        return result
    }
}