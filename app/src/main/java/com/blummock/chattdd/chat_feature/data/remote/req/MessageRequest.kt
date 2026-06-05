package com.blummock.chattdd.chat_feature.data.remote.req

internal data class MessageRequest(
    val clientMessageId: String,
    val chatId: String,
    val text: String?,
)
