package com.blummock.chattdd.chat_feature.domain

import com.blummock.chattdd.chat_feature.domain.entity.ChatResult
import com.blummock.chattdd.chat_feature.domain.entity.DomainError
import com.blummock.chattdd.chat_feature.domain.entity.Message
import com.blummock.chattdd.chat_feature.domain.entity.MessagesState
import com.blummock.chattdd.chat_feature.domain.entity.TextMessage
import com.blummock.chattdd.chat_feature.domain.repositories.MessagesRepository
import com.blummock.chattdd.chat_feature.domain.use_cases.SendMessageUseCase
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SendMessageUseCaseTests {

    @Test
    fun `success scenario`() = runTest {
        val repository = FakeMessagesRepository()
        val useCase = SendMessageUseCase(repository)
        val message = TextMessage(
            id = "tamquam",
            chatId = "nonumy",
            senderId = "dictumst",
            timestamp = 1972,
            status = Message.MessageStatus.SENDING,
            isMine = false,
            text = "accusata"
        )

        repository.result = ChatResult.Success(Unit)
        assertEquals(ChatResult.Success(Unit), useCase(message))
    }

    @Test
    fun `fail scenario`() = runTest {
        val repository = FakeMessagesRepository()
        val useCase = SendMessageUseCase(repository)
        val message = TextMessage(
            id = "tamquam",
            chatId = "nonumy",
            senderId = "dictumst",
            timestamp = 1972,
            status = Message.MessageStatus.SENDING,
            isMine = false,
            text = "accusata"
        )

        val expectedError = ChatResult.Error(DomainError.UnknownError)
        repository.result = expectedError
        assertEquals(expectedError, useCase(message))
    }

    private class FakeMessagesRepository : MessagesRepository {

        lateinit var result: ChatResult<Unit>

        override fun observeMessages(): Flow<MessagesState> {
            return emptyFlow()
        }

        override suspend fun postMessage(message: Message): ChatResult<Unit> {
            return result
        }
    }
}
