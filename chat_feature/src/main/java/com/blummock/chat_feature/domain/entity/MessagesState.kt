package com.blummock.chat_feature.domain.entity

sealed interface MessagesState {

    data class Data(
        val messages: List<Message>
    ) : MessagesState

    data class Error(
        val reason: DomainError
    ) : MessagesState
}