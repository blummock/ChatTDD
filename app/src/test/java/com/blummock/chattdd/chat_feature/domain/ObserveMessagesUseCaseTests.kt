package com.blummock.chattdd.chat_feature.domain

import com.blummock.chattdd.chat_feature.domain.entity.ChatResult
import com.blummock.chattdd.chat_feature.domain.entity.DomainError
import com.blummock.chattdd.chat_feature.domain.entity.Message
import com.blummock.chattdd.chat_feature.domain.entity.MessagesState
import com.blummock.chattdd.chat_feature.domain.entity.TextMessage
import com.blummock.chattdd.chat_feature.domain.repositories.MessagesRepository
import com.blummock.chattdd.chat_feature.domain.use_cases.ObserveMessagesUseCase
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ObserveMessagesUseCaseTests {

    private lateinit var repository:FakeMessagesRepository
    private lateinit var useCase: ObserveMessagesUseCase

    @Before
    fun setup() {
        repository = FakeMessagesRepository()
        useCase = ObserveMessagesUseCase(repository)
    }

    @Test
    fun `success scenario`() = runTest {
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
        repository.messagesFlow = flowOf(MessagesState.Data(expectedMessages))
        assertEquals(MessagesState.Data(expectedMessages), useCase().first())
        val expectedError = DomainError.UnknownError
        repository.messagesFlow = flowOf(MessagesState.Error(expectedError))
        assertEquals(MessagesState.Error(expectedError), useCase().first())
    }

    @Test
    fun `fail scenario`() = runTest {
        val expectedError = DomainError.UnknownError
        repository.messagesFlow = flowOf(MessagesState.Error(expectedError))
        assertEquals(MessagesState.Error(expectedError), useCase().first())
    }

    private class FakeMessagesRepository : MessagesRepository {

        lateinit var messagesFlow: Flow<MessagesState>

        override fun observeMessages(): Flow<MessagesState> {
            return messagesFlow
        }

        override suspend fun postTextMessage(message: String): ChatResult<Unit> {
            return ChatResult.Success(Unit)
        }
    }
}

