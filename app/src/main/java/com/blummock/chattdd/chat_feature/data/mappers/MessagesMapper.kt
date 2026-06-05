package com.blummock.chattdd.chat_feature.data.mappers


import com.blummock.chattdd.chat_feature.data.local.models.MessageEntity
import com.blummock.chattdd.chat_feature.data.remote.dto.MessageDto
import com.blummock.chattdd.chat_feature.data.remote.dto.MessageStatus
import com.blummock.chattdd.chat_feature.data.remote.dto.MessageType
import com.blummock.chattdd.chat_feature.domain.entity.Message
import com.blummock.chattdd.chat_feature.domain.entity.TextMessage

internal class MessagesMapper {

    fun toLocal(dto: MessageDto) = when (dto.type) {
        MessageType.TEXT -> MessageEntity(
            id = dto.id,
            chatId = dto.chatId,
            senderId = dto.senderId,
            timestamp = dto.timestamp,
            type = dto.type.map(),
            status = dto.status.map(),
            text = dto.text,
            imageUrl = dto.imageUrl
        )
    }

    fun toDomain(entity: MessageEntity, userId: String) = when (entity.type) {
        MessageEntity.MessageType.TEXT -> TextMessage(
            id = entity.id,
            chatId = entity.chatId,
            senderId = entity.senderId,
            timestamp = entity.timestamp,
            status = entity.status.map(),
            isMine = userId == entity.senderId,
            text = entity.text ?: ""
        )
    }

    private fun MessageType.map() = when (this) {
        MessageType.TEXT -> MessageEntity.MessageType.TEXT
    }

    private fun MessageStatus.map() = when (this) {
        MessageStatus.SENDING -> MessageEntity.MessageStatus.SENDING
        MessageStatus.DELIVERED -> MessageEntity.MessageStatus.DELIVERED
        MessageStatus.ERROR -> MessageEntity.MessageStatus.ERROR
    }

    private fun MessageEntity.MessageStatus.map() = when (this) {
        MessageEntity.MessageStatus.SENDING -> Message.MessageStatus.SENDING
        MessageEntity.MessageStatus.DELIVERED -> Message.MessageStatus.DELIVERED
        MessageEntity.MessageStatus.ERROR -> Message.MessageStatus.ERROR
    }
}