package com.blummock.chattdd.chat_feature.domain

import com.blummock.chattdd.chat_feature.domain.entity.ChatResult
import com.blummock.chattdd.chat_feature.domain.entity.DomainError
import com.blummock.chattdd.chat_feature.domain.entity.Message
import com.blummock.chattdd.chat_feature.domain.entity.MessagesState
import com.blummock.chattdd.chat_feature.domain.entity.TextMessage
import com.blummock.chattdd.chat_feature.domain.repositories.MessagesRepository
import com.blummock.chattdd.chat_feature.domain.use_cases.ObserveMessagesUseCase
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ObserveMessagesUseCaseTests {

    @Test
    fun scenario() = runTest {
        val repository = FakeMessagesRepository()
        val useCase = ObserveMessagesUseCase(repository)
        val messages = useCase().stateIn(this, started = SharingStarted.Eagerly, null)
        assertNull(messages)
        val expectedMessages = listOf(
            TextMessage(
                id = "tamquam",
                chatId = "nonumy",
                senderId = "dictumst",
                timestamp = 1972,
                status = Message.MessageStatus.SENDING,
                isMine = false,
                text = "accusata"
            ),
            TextMessage(
                id = "elementum",
                chatId = "expetendis",
                senderId = "iusto",
                timestamp = 5834,
                status = Message.MessageStatus.SENDING,
                isMine = false,
                text = "omittam"
            )
        )
        repository.messagesState.emit(MessagesState.Data(expectedMessages))
        assertEquals(MessagesState.Data(expectedMessages), messages.value)
        val expectedError = DomainError.UnknownError
        repository.messagesState.emit(MessagesState.Error(expectedError))
        assertEquals(MessagesState.Error(expectedError), messages.value)
    }

    private class FakeMessagesRepository : MessagesRepository {

        val messagesState = MutableSharedFlow<MessagesState>()

        override fun observeMessages(): Flow<MessagesState> {
            return messagesState
        }

        override suspend fun postMessage(message: Message): ChatResult<Unit> {
            return ChatResult.Success(Unit)
        }
    }
}

