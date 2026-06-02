package com.blummock.chat_feature.data.mappers

import com.blummock.chat_feature.data.dto.MessageDto
import com.blummock.chat_feature.data.dto.MessageStatus
import com.blummock.chat_feature.data.dto.MessageType
import com.blummock.chat_feature.data.req.MessageRequest
import com.blummock.chat_feature.domain.entity.Message
import com.blummock.chat_feature.domain.entity.TextMessage
import java.util.UUID

internal class MessagesMapper {

    fun map(messageDto: MessageDto): Message {
        when (messageDto.type) {
            MessageType.TEXT -> {
                return TextMessage(
                    id = messageDto.id,
                    chatId = messageDto.chatId,
                    senderId = messageDto.senderId,
                    timestamp = messageDto.timestamp,
                    status = messageDto.status.toDomain(),
                    text = messageDto.text ?: "",
                )
            }
        }
    }

    fun MessageStatus.toDomain() = when (this) {
        MessageStatus.SENDING -> Message.MessageStatus.SENDING
        MessageStatus.SENT -> Message.MessageStatus.SENT
        MessageStatus.DELIVERED -> Message.MessageStatus.DELIVERED
        MessageStatus.ERROR -> Message.MessageStatus.ERROR
    }

    fun mapToRequest(message: Message): MessageRequest {
        return MessageRequest(
            clientMessageId = UUID.randomUUID().toString(),
            chatId = message.chatId,
            text = (message as? TextMessage)?.text
        )
    }
}