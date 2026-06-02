package com.blummock.chat_feature.domain.use_cases

import com.blummock.chat_feature.domain.entity.ChatResult
import com.blummock.chat_feature.domain.entity.Message
import com.blummock.chat_feature.domain.repositories.MessagesRepository

class SendMessageUseCase(
    val messagesRepository: MessagesRepository
) {
    suspend operator fun invoke(message: Message): ChatResult<Unit> {
        return messagesRepository.postMessage(message)
    }
}