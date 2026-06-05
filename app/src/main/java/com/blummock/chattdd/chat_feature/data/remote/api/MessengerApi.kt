package com.blummock.chattdd.chat_feature.data.remote.api

//import com.blummock.chattdd.chat_feature.data.dto.MessageDto
//import com.blummock.chattdd.chat_feature.data.dto.MessageStatus
//import com.blummock.chattdd.chat_feature.data.dto.MessageType
//import com.blummock.chattdd.chat_feature.data.reps.SendMessageResponse
//import com.blummock.chattdd.chat_feature.data.req.MessageRequest
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.flowOf

//internal class MessengerApi {
//
//    fun observeMessages(): Flow<List<MessageDto>> {
//        return flowOf(
//            listOf(
//                MessageDto(
//                    id = "0",
//                    text = "Hi",
//                    chatId = "tritani",
//                    senderId = "nihil",
//                    timestamp = 7417,
//                    type = MessageType.TEXT,
//                    status = MessageStatus.SENDING,
//                    imageUrl = "https://duckduckgo.com/?q=adhuc",
//                ),
//                MessageDto(
//                    id = "1",
//                    text = "How are you ?",
//                    chatId = "eam",
//                    senderId = "pertinacia",
//                    timestamp = 6987,
//                    type = MessageType.TEXT,
//                    status = MessageStatus.SENDING,
//                    imageUrl = "https://duckduckgo.com/?q=deseruisse",
//                )
//            )
//        )
//    }
//
//    suspend fun postMessage(message: MessageRequest): SendMessageResponse {
//        return SendMessageResponse(messageId = "eos", clientMessageId = "dico", timestamp = 6973, status = "nisi")
//    }
//}