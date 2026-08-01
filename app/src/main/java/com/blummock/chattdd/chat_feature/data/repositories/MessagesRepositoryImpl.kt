package com.blummock.chattdd.chat_feature.data.repositories

import com.blummock.chattdd.chat_feature.data.local.dao.MessagesDao
import com.blummock.chattdd.chat_feature.data.mappers.ExceptionMapper
import com.blummock.chattdd.chat_feature.data.mappers.MessagesMapper
import com.blummock.chattdd.chat_feature.data.remote.api.MessagesApi
import com.blummock.chattdd.chat_feature.data.remote.req.MessageRequest
import com.blummock.chattdd.chat_feature.domain.entity.ChatResult
import com.blummock.chattdd.chat_feature.domain.entity.DomainError
import com.blummock.chattdd.chat_feature.domain.entity.MessagesState
import com.blummock.chattdd.chat_feature.domain.repositories.MessagesRepository
import com.blummock.chattdd.chat_feature.domain.repositories.UserInfoRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

internal class MessagesRepositoryImpl(
    private val messagesDao: MessagesDao,
    private val messagesApi: MessagesApi,
    private val userInfoRepository: UserInfoRepository,
    private val mapper: MessagesMapper,
    private val exceptionMapper: ExceptionMapper,
    private val dispatcher: CoroutineDispatcher,
) : MessagesRepository {

    override fun observeMessages(): Flow<MessagesState> = flow {
        when (val userInfo = userInfoRepository.getUserInfo()) {
            is ChatResult.Error -> emit(MessagesState.Error(userInfo.error))
            is ChatResult.Success -> emitAll(collectMessages(userInfo.data.userId))
        }
    }.flowOn(dispatcher)

    private fun collectMessages(userId: String) = channelFlow {
        val errorStateFlow = MutableStateFlow<DomainError?>(null)
        launch {
            errorStateFlow.combine(messagesDao.observeMessages()) { errorState, messages ->
                val domainMessages = messages.map { mapper.toDomain(it, userId) }
                if (errorState == null) {
                    MessagesState.Data(domainMessages)
                } else {
                    MessagesState.Error(errorState, domainMessages)
                }
            }.collect { send(it) }
        }
        launch {
            try {
                messagesApi.observeMessages().collect { messages ->
                    messagesDao.clearAndInsertMessages(messages.map { mapper.toLocal(it) })
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                errorStateFlow.value = exceptionMapper.map(e)
            }
        }
    }

    override suspend fun postTextMessage(message: String): ChatResult<Unit> {
        return try {
            val messageRequest = MessageRequest(
                clientMessageId = UUID.randomUUID().toString(),
                text = message,
            )
            withContext(dispatcher) {
                messagesApi.postMessage(messageRequest)
            }
            ChatResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ChatResult.Error(exceptionMapper.map(e))
        }
    }
}