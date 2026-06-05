package com.blummock.chattdd.chat_feature.data.local.models

import com.blummock.chattdd.chat_feature.data.remote.dto.MessageStatus
import com.blummock.chattdd.chat_feature.data.remote.dto.MessageType

internal data class MessageEntity(
    val id: String,
    val chatId: String,
    val senderId: String,
    val timestamp: Long,
    val type: MessageType,
    val status: MessageStatus,
    val text: String? = null,
    val imageUrl: String? = null,
)

internal enum class MessageType {
    TEXT,
}

internal enum class MessageStatus {
    SENDING,
    DELIVERED,
    ERROR,
}


