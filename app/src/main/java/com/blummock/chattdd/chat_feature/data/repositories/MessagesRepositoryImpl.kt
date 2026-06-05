package com.blummock.chattdd.chat_feature.data.repositories

//import com.blummock.chattdd.chat_feature.data.api.MessengerApi
//import com.blummock.chattdd.chat_feature.data.mappers.ExceptionMapper
//import com.blummock.chattdd.chat_feature.data.mappers.MessagesMapper
//import com.blummock.chattdd.chat_feature.domain.entity.ChatResult
//import com.blummock.chattdd.chat_feature.domain.entity.Message
//import com.blummock.chattdd.chat_feature.domain.entity.MessagesState
//import com.blummock.chattdd.chat_feature.domain.repositories.MessagesRepository
//import kotlinx.coroutines.CancellationException
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.flowOf
//import kotlinx.coroutines.flow.map

//internal class MessagesRepositoryImpl(
//    val api: MessengerApi,
//    val mapper: MessagesMapper,
//    val exceptionMapper: ExceptionMapper,
//) : MessagesRepository {
//    override fun observeMessages(): Flow<MessagesState> {
//
//        return try {
//            api.observeMessages().map { dto ->
//                MessagesState.Data(messages = dto.map { mapper.map(it) })
//            }
//        } catch (e: Exception) {
//            flowOf(MessagesState.Error(exceptionMapper.map(e)))
//        }
//    }
//
//    override suspend fun postMessage(message: Message): ChatResult<Unit> {
//        return try {
//            api.postMessage(mapper.mapToRequest(message))
//            ChatResult.Success(Unit)
//        } catch (e: CancellationException) {
//            throw e
//        } catch (e: Exception) {
//            ChatResult.Error(exceptionMapper.map(e))
//        }
//    }
//}