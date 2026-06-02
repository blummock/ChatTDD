package com.blummock.chat_feature.data.req

data class MessageRequest(
    val clientMessageId: String,
    val chatId: String,
    val text: String?,
)
