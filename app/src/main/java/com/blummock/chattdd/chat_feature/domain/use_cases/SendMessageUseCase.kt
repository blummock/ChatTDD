package com.blummock.chattdd.chat_feature.domain.use_cases

import com.blummock.chattdd.chat_feature.domain.entity.ChatResult
import com.blummock.chattdd.chat_feature.domain.repositories.MessagesRepository

class SendMessageUseCase(
    val messagesRepository: MessagesRepository
) {
    suspend operator fun invoke(message: String): ChatResult<Unit> {
        return messagesRepository.postTextMessage(message)
    }
}