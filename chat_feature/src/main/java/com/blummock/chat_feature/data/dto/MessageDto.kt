package com.blummock.chat_feature.data.dto

internal data class MessageDto(
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
    SENT,
    DELIVERED,
    ERROR,
}