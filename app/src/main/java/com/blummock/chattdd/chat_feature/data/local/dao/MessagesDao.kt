package com.blummock.chattdd.chat_feature.data.local.dao

import com.blummock.chattdd.chat_feature.data.local.models.MessageEntity
import kotlinx.coroutines.flow.Flow

internal interface MessagesDao {

    fun observeMessages(): Flow<List<MessageEntity>>

    suspend fun clearAndInsertMessages(messages: List<MessageEntity>)
}