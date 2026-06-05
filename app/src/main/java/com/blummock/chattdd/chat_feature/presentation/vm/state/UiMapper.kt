package com.blummock.chattdd.chat_feature.presentation.vm.state

import com.blummock.chattdd.chat_feature.core.TimeConverter
import com.blummock.chattdd.chat_feature.domain.entity.Message
import com.blummock.chattdd.chat_feature.domain.entity.TextMessage

internal class UiMapper(
    private val timeConverter: TimeConverter
) {
    fun toUi(message: Message) = when (message) {
        is TextMessage -> TextMessageModel(
            id = message.id,
            time = timeConverter.toHHmm(message.timestamp),
            isMine = message.isMine,
            status = message.status.map(),
            text = message.text
        )
    }

    private fun Message.MessageStatus.map() = when (this) {
        Message.MessageStatus.SENDING -> MessageStatusUi.SENDING
        Message.MessageStatus.DELIVERED -> MessageStatusUi.DELIVERED
        Message.MessageStatus.ERROR -> MessageStatusUi.ERROR
    }
}