package com.blummock.chat_feature.domain.use_cases

import com.blummock.chat_feature.domain.repositories.MessagesRepository

class ObserveMessagesUseCase(
    val messagesRepository: MessagesRepository,
) {
    operator fun invoke() = messagesRepository.observeMessages()
}