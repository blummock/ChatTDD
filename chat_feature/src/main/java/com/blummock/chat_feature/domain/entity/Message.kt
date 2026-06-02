package com.blummock.chat_feature.domain.entity

sealed interface Message {
    val id: String
    val chatId: String
    val senderId: String
    val timestamp: Long
    val status: MessageStatus

    enum class MessageStatus {
        SENDING,
        SENT,
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
    val text: String,
) : Message
