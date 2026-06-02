package com.blummock.chat_feature.domain.repositories

import com.blummock.chat_feature.domain.entity.ChatResult
import com.blummock.chat_feature.domain.entity.Message
import com.blummock.chat_feature.domain.entity.MessagesState
import kotlinx.coroutines.flow.Flow

interface MessagesRepository {

    fun observeMessages(): Flow<MessagesState>
    suspend fun postMessage(message: Message): ChatResult<Unit>
}