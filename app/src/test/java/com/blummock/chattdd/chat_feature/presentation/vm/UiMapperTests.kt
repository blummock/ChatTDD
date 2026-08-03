package com.blummock.chattdd.chat_feature.presentation.vm

import com.blummock.chattdd.chat_feature.core.TimeConverter
import com.blummock.chattdd.chat_feature.domain.entity.Message
import com.blummock.chattdd.chat_feature.domain.entity.TextMessage
import com.blummock.chattdd.chat_feature.presentation.vm.state.MessageStatusUi
import com.blummock.chattdd.chat_feature.presentation.vm.state.TextMessageModel
import junit.framework.TestCase.assertEquals
import org.junit.Test

class UiMapperTests {

    @Test
    fun `map TextMessage to TextMessageModel`() {
        val mapper = UiMapper(
            timeConverter = TimeConverter.Base(),
        )
        val domainMessages = listOf<Message>(
            TextMessage(
                id = "delectus",
                chatId = "mediocrem",
                senderId = "user1",
                timestamp = 1780457123,
                isMine = false,
                status = Message.MessageStatus.DELIVERED,
                text = "ignota"
            ),
            TextMessage(
                id = "congue",
                chatId = "eloquentiam",
                senderId = "arcu",
                timestamp = 1780457123,
                isMine = true,
                status = Message.MessageStatus.SENDING,
                text = "veniam"
            )
        )
        val expected = listOf(
            TextMessageModel(
                id = "delectus",
                isMine = false,
                time = "14:33",
                status = MessageStatusUi.DELIVERED,
                text = "ignota"
            ),
            TextMessageModel(
                id = "congue",
                isMine = true,
                time = "14:34",
                status = MessageStatusUi.SENDING,
                text = "veniam"
            )
        )
        assertEquals(expected, domainMessages.map { mapper.toUi(it) })
    }
}