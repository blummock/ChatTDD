package com.blummock.chattdd.chat_feature.data.local.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
internal data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val senderId: String,
    val timestamp: Long,
    val type: MessageType,
    val status: MessageStatus,
    val text: String? = null,
    val imageUrl: String? = null,
) {
    internal enum class MessageType {
        TEXT,
    }

    internal enum class MessageStatus {
        SENDING,
        DELIVERED,
        ERROR,
    }
}


