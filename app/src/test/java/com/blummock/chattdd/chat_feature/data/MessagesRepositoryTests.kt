package com.blummock.chattdd.chat_feature.data

import com.blummock.chattdd.chat_feature.data.local.dao.MessagesDao
import com.blummock.chattdd.chat_feature.data.local.models.MessageEntity
import com.blummock.chattdd.chat_feature.data.mappers.ExceptionMapper
import com.blummock.chattdd.chat_feature.data.mappers.MessagesMapper
import com.blummock.chattdd.chat_feature.data.remote.api.MessagesApi
import com.blummock.chattdd.chat_feature.data.remote.dto.MessageDto
import com.blummock.chattdd.chat_feature.data.remote.dto.MessageStatus
import com.blummock.chattdd.chat_feature.data.remote.dto.MessageType
import com.blummock.chattdd.chat_feature.data.remote.reps.SendMessageResponse
import com.blummock.chattdd.chat_feature.data.remote.req.MessageRequest
import com.blummock.chattdd.chat_feature.data.repositories.MessagesRepositoryImpl
import com.blummock.chattdd.chat_feature.domain.entity.ChatResult
import com.blummock.chattdd.chat_feature.domain.entity.DomainError
import com.blummock.chattdd.chat_feature.domain.entity.MessagesState
import com.blummock.chattdd.chat_feature.domain.entity.UserInfo
import com.blummock.chattdd.chat_feature.domain.repositories.MessagesRepository
import com.blummock.chattdd.chat_feature.domain.repositories.UserInfoRepository
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MessagesRepositoryTest {

    private lateinit var fakeMessagesDao: FakeMessagesDao
    private lateinit var fakeMessagesApi: FakeMessagesApi
    private lateinit var fakeUserInfoRepository: FakeUserInfoRepository
    private lateinit var messagesRepository: MessagesRepository
    private lateinit var mapper: MessagesMapper
    private lateinit var exceptionMapper: ExceptionMapper
    private val scheduler = TestCoroutineScheduler()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcher = UnconfinedTestDispatcher(scheduler)

    @Before
    fun setup() {
        fakeMessagesDao = FakeMessagesDao()
        fakeMessagesApi = FakeMessagesApi()
        fakeUserInfoRepository = FakeUserInfoRepository()
        mapper = MessagesMapper()
        exceptionMapper = ExceptionMapper()
        messagesRepository = MessagesRepositoryImpl(
            messagesDao = fakeMessagesDao,
            messagesApi = fakeMessagesApi,
            userInfoRepository = fakeUserInfoRepository,
            mapper = mapper,
            exceptionMapper = exceptionMapper,
            dispatcher = dispatcher
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `When loading messages then they come from cache first and then from api`() = runTest(dispatcher) {
        val entities = getLocalMessages()
        fakeMessagesDao.flow.emit(entities)

        val messages = mutableListOf<MessagesState>()
        backgroundScope.launch {
            messagesRepository.observeMessages().toList(messages)
        }

        val userId = fakeUserInfoRepository.getUserInfo().data.userId
        val expected = MessagesState.Data(entities.map { mapper.toDomain(it, userId) })
        assertEquals(expected, messages[0])

        val dto = getRemoteMessages()
        fakeMessagesApi.flow.emit(dto)
        val expected2 = listOf(dto.map { mapper.toLocal(it) })
        assertEquals(expected2, fakeMessagesDao.updates)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `When loading messages with api error then they come from cache`() = runTest(dispatcher) {
        fakeMessagesApi.error = RuntimeException()
        val entities = getLocalMessages()
        fakeMessagesDao.flow.emit(entities)

        val messages = mutableListOf<MessagesState>()
        backgroundScope.launch {
            messagesRepository.observeMessages().toList(messages)
        }

        val userId = fakeUserInfoRepository.getUserInfo().data.userId
        val expected = MessagesState.Error(DomainError.UnknownError, entities.map { mapper.toDomain(it, userId) })
        assertEquals(expected, messages[0])
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `When loading messages and cache is empty then wait load from api`() = runTest(dispatcher) {
        val messages = mutableListOf<MessagesState>()
        backgroundScope.launch {
            messagesRepository.observeMessages().toList(messages)
        }

        val entities = getLocalMessages()
        fakeMessagesDao.flow.emit(entities)
        assertTrue(messages.isEmpty())

        val dto = getRemoteMessages()
        fakeMessagesApi.flow.emit(dto)
        val expected = listOf(dto.map { mapper.toLocal(it) })
        assertEquals(expected, fakeMessagesDao.updates)

        val userId = fakeUserInfoRepository.getUserInfo().data.userId
        val expected2 = MessagesState.Data(entities.map { mapper.toDomain(it, userId) })
        assertEquals(expected2, messages[0])

        fakeMessagesDao.flow.emit(emptyList())
        assertEquals(MessagesState.Data(emptyList()), messages[1])
    }

    @Test
    fun `When post message with success then the result is success`() = runTest(dispatcher) {
        val result = messagesRepository.postTextMessage("sdsfds")
        assertEquals(ChatResult.Success(Unit), result)
    }

    @Test
    fun `When post message with error then result is fail`() = runTest(dispatcher) {
        fakeMessagesApi.error = RuntimeException()
        val result = messagesRepository.postTextMessage("dscrwr")
        assertEquals(ChatResult.Error(DomainError.UnknownError), result)
    }
}

private fun getLocalMessages() = listOf(
    MessageEntity(
        id = "noster",
        chatId = "alterum",
        senderId = "posidonium",
        timestamp = 9171,
        type = MessageEntity.MessageType.TEXT,
        status = MessageEntity.MessageStatus.SENDING,
        text = "persecuti",
        imageUrl = "https://www.google.com/#q=cursus"
    )
)

private fun getRemoteMessages() = listOf(
    MessageDto(
        id = "delectus",
        chatId = "inani",
        senderId = "quidam",
        timestamp = 6450,
        type = MessageType.TEXT,
        status = MessageStatus.SENDING,
        text = "atomorum",
        imageUrl = "https://www.google.com/#q=principes"
    )
)

private class FakeMessagesDao() : MessagesDao {

    val updates = mutableListOf<List<MessageEntity>>()

    val flow = MutableStateFlow<List<MessageEntity>>(emptyList())

    override fun observeMessages(): Flow<List<MessageEntity>> {
        return flow
    }

    override suspend fun clearAndInsertMessages(messages: List<MessageEntity>) {
        updates.add(messages)
    }
}

private class FakeMessagesApi() : MessagesApi {

    val flow = MutableSharedFlow<List<MessageDto>>()
    var error: Throwable? = null

    override fun observeMessages(): Flow<List<MessageDto>> {
        error?.let {
            throw it
        }
        return flow
    }

    override suspend fun postMessage(message: MessageRequest): SendMessageResponse {
        error?.let {
            throw it
        }
        return SendMessageResponse(
            messageId = "dapibus",
            clientMessageId = "repudiandae",
            timestamp = 3573,
            status = "utamur"
        )
    }
}

private class FakeUserInfoRepository() : UserInfoRepository {
    override suspend fun getUserInfo() = ChatResult.Success(UserInfo("user1"))
}