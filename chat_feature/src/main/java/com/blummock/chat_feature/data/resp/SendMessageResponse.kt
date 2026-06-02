package com.blummock.chat_feature.data.resp

data class SendMessageResponse(
    val messageId: String,
    val clientMessageId: String,
    val timestamp: Long,
    val status: String
)