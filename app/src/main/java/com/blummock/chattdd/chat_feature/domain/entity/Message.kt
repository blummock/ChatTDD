package com.blummock.chattdd.chat_feature.domain.entity

sealed interface Message {
    val id: String
    val chatId: String
    val senderId: String
    val timestamp: Long
    val status: MessageStatus
    val isMine: Boolean

    enum class MessageStatus {
        SENDING,
        DELIVERED,
        ERROR,
    }
}

data class TextMessage(
    override val id: String,
    override val chatId: String,
    override val senderId: String,
    override val timestamp: Long,
    override val status: Message.MessageStatus,
    override val isMine: Boolean,
    val text: String,
) : Message
