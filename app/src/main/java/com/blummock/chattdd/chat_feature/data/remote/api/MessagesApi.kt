package com.blummock.chattdd.chat_feature.data.remote.api

import com.blummock.chattdd.chat_feature.data.remote.dto.MessageDto
import com.blummock.chattdd.chat_feature.data.remote.reps.SendMessageResponse
import com.blummock.chattdd.chat_feature.data.remote.req.MessageRequest
import kotlinx.coroutines.flow.Flow

internal interface MessagesApi {

    fun observeMessages(): Flow<List<MessageDto>>

    suspend fun postMessage(message: MessageRequest): SendMessageResponse
}