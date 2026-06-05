package com.blummock.chattdd.chat_feature.data

import com.blummock.chattdd.chat_feature.data.local.models.MessageEntity
import com.blummock.chattdd.chat_feature.data.remote.dto.MessageDto
import com.blummock.chattdd.chat_feature.data.remote.dto.MessageStatus
import com.blummock.chattdd.chat_feature.data.remote.dto.MessageType
import com.blummock.chattdd.chat_feature.domain.entity.Message
import com.blummock.chattdd.chat_feature.domain.entity.TextMessage
import junit.framework.TestCase.assertEquals
import org.junit.Test

class MessagesMapperTests {

    @Test
    fun `map local to domain`() {
        val entity = MessageEntity(
            id = "nullam",
            chatId = "sociis",
            senderId = "turpis",
            timestamp = 2001,
            type = MessageEntity.MessageType.TEXT,
            status = MessageEntity.MessageStatus.ERROR,
            text = "metus",
            imageUrl = ""
        )
        val expected = TextMessage(
            id = "nullam",
            chatId = "sociis",
            senderId = "turpis",
            timestamp = 2001,
            status = Message.MessageStatus.ERROR,
            isMine = true,
            text = "metus",
        )
        assertEquals(expected, MessagesMapper().toDomain(entity, "nullam"))
        val entity2 = MessageEntity(
            id = "nullam",
            chatId = "sociis",
            senderId = "turpis",
            timestamp = 2001,
            type = MessageEntity.MessageType.TEXT,
            status = MessageEntity.MessageStatus.DELIVERED,
            text = "metus",
            imageUrl = ""
        )
        val expected2 = TextMessage(
            id = "nullam",
            chatId = "sociis",
            senderId = "turpis",
            timestamp = 2001,
            status = Message.MessageStatus.DELIVERED,
            isMine = false,
            text = "metus",
        )
        assertEquals(expected2, MessagesMapper().toDomain(entity2, "user2"))
    }

    @Test
    fun `map remote to local`() {
        val dto = MessageDto(
            id = "quem",
            chatId = "elit",
            senderId = "adolescens",
            timestamp = 3651,
            type = MessageType.TEXT,
            status = MessageStatus.SENDING,
            text = "laudem",
            imageUrl = "https://duckduckgo.com/?q=similique"
        )
        val expected = MessageEntity(
            id = "quem",
            chatId = "elit",
            senderId = "adolescens",
            timestamp = 3651,
            type = MessageEntity.MessageType.TEXT,
            status = MessageEntity.MessageStatus.SENDING,
            text = "laudem",
            imageUrl = "https://duckduckgo.com/?q=similique"
        )
        assertEquals(expected, MessagesMapper().toLocal(dto))
    }
}