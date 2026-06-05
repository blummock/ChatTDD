package com.blummock.chattdd.chat_feature.data.remote.reps

internal data class SendMessageResponse(
    val messageId: String,
    val clientMessageId: String,
    val timestamp: Long,
    val status: String
)