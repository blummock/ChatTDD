package com.blummock.chattdd.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import com.blummock.chattdd.chat_feature.presentation.vm.state.MessageUiModel
import com.blummock.chattdd.chat_feature.presentation.vm.state.TextMessageModel

internal class ChatScreenPage(private val rule: ComposeContentTestRule) {

    private val sendButton get() = rule.onNodeWithTag("sendButton")
    private val textInput get() = rule.onNodeWithTag("textInput")
    private val loadingList get() = rule.onNodeWithTag("loadingList")
    private val messagesList get() = rule.onNodeWithTag("messagesList")
    private val toast get() = rule.onNodeWithTag("toast")
    private val emptyList get() = rule.onNodeWithTag("emptyList")

    fun typeTextInput(text: String): ChatScreenPage {
        textInput.performTextReplacement(text)
        return this
    }

    fun clickSendButton(): ChatScreenPage {
        sendButton.performClick()
        return this
    }

    fun assertSendButtonDisabled(): ChatScreenPage {
        sendButton.assertIsNotEnabled()
        return this
    }

    fun assertSendButtonEnabled(): ChatScreenPage {
        sendButton.assertIsEnabled()
        return this
    }

    fun assertTextInputExists(): ChatScreenPage {
        textInput.assertExists()
        return this
    }

    fun assertTextInputEquals(text: String): ChatScreenPage {
        textInput.assertTextEquals(text)
        return this
    }

    fun assertToastNotExists(): ChatScreenPage {
        toast.assertDoesNotExist()
        return this
    }

    fun assertToastMessage(message: String): ChatScreenPage {
        rule.onNode(
            hasTestTag("toast") and
                    hasAnyAncestor(hasText(message))
        ).assertExists()
        return this
    }

    fun assertLoadingExists(): ChatScreenPage {
        loadingList.assertExists()
        return this
    }

    fun assertMessagesListNotExists(): ChatScreenPage {
        messagesList.assertDoesNotExist()
        return this
    }

    fun assertEmptyListNotExists(): ChatScreenPage {
        emptyList.assertDoesNotExist()
        return this
    }

    fun assertEmptyListExists(): ChatScreenPage {
        emptyList.assertExists()
        return this
    }

    fun assertMessageAtPosition(position: Int, message: MessageUiModel) {
        messagesList
            .performScrollToNode(hasTestTag("Element at $position"))
            .assertIsDisplayed()
        when (message) {
            is TextMessageModel -> {
                rule.onNode(
                    hasTestTag("Element at $position") and
                            hasAnyAncestor(hasTestTag("whoos ${message.isMine}")) and
                            hasAnyDescendant(hasText(message.text)) and
                            hasAnyDescendant(hasTestTag("status ${message.status}")) and
                            hasAnyDescendant(hasText(message.time))
                ).assertExists()
            }
        }
    }

    fun scrollListToPosition(position: Int): ChatScreenPage {
        messagesList.performScrollToNode(hasTestTag("Element at $position"))
        return this
    }

    fun assertElementAtNotDisplayed(position: Int): ChatScreenPage {
        rule.onNodeWithTag("Element at $position").assertIsNotDisplayed()
        return this
    }

    fun assertElementAtDisplayed(position: Int): ChatScreenPage {
        rule.onNodeWithTag("Element at $position").assertIsDisplayed()
        return this
    }
}