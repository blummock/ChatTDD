package com.blummock.chattdd.chat_feature.domain

import com.blummock.chattdd.chat_feature.domain.entity.ChatResult
import com.blummock.chattdd.chat_feature.domain.entity.DomainError
import com.blummock.chattdd.chat_feature.domain.entity.MessagesState
import com.blummock.chattdd.chat_feature.domain.repositories.MessagesRepository
import com.blummock.chattdd.chat_feature.domain.use_cases.SendMessageUseCase
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SendMessageUseCaseTests {

    private lateinit var repository: FakeMessagesRepository
    private lateinit var useCase: SendMessageUseCase

    @Before
    fun setup() {
        repository = FakeMessagesRepository()
        useCase = SendMessageUseCase(repository)
    }

    @Test
    fun `success scenario`() = runTest {
        repository.result = ChatResult.Success(Unit)
        assertEquals(ChatResult.Success(Unit), useCase("accusata"))
    }

    @Test
    fun `fail scenario`() = runTest {
        val expectedError = ChatResult.Error(DomainError.UnknownError)
        repository.result = expectedError
        assertEquals(expectedError, useCase("accusata"))
    }

    private class FakeMessagesRepository : MessagesRepository {

        lateinit var result: ChatResult<Unit>

        override fun observeMessages(): Flow<MessagesState> {
            return emptyFlow()
        }

        override suspend fun postTextMessage(message: String): ChatResult<Unit> {
            return result
        }
    }
}
