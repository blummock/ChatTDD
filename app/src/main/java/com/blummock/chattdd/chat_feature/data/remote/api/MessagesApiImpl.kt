package com.blummock.chattdd.chat_feature.data.remote.api

import com.blummock.chattdd.chat_feature.data.remote.dto.MessageDto
import com.blummock.chattdd.chat_feature.data.remote.dto.MessageStatus
import com.blummock.chattdd.chat_feature.data.remote.dto.MessageType
import com.blummock.chattdd.chat_feature.data.remote.reps.SendMessageResponse
import com.blummock.chattdd.chat_feature.data.remote.req.MessageRequest
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import java.io.IOException
import java.time.ZonedDateTime
import java.util.UUID
import kotlin.random.Random

internal class MessagesApiImpl : MessagesApi {

    private val store = MutableStateFlow(emptyList<MessageDto>())

    @OptIn(FlowPreview::class)
    override fun observeMessages(): Flow<List<MessageDto>> = flow{
        delay(1000)
        emitAll(store.asStateFlow())
    }

    override suspend fun postMessage(message: MessageRequest): SendMessageResponse {
        if (Random.nextBoolean()) throw IOException()
        delay(200)
        val newMessage = MessageDto(
            id = UUID.randomUUID().toString(),
            chatId = UUID.randomUUID().toString(),
            senderId = "suavitate",
            timestamp = ZonedDateTime.now().toInstant().epochSecond,
            type = MessageType.TEXT,
            status = MessageStatus.SENDING,
            text = message.text,
            imageUrl = "",
        )
        store.update { it + newMessage }
        return SendMessageResponse(
            messageId = "et", clientMessageId = "lorem", timestamp = 1847, status = "suas"
        )
    }
}