package com.blummock.chattdd.chat_feature.domain.entity

sealed interface MessagesState {

    data class Data(
        val messages: List<Message>
    ) : MessagesState

    data class Error(
        val reason: DomainError,
        val messages: List<Message> = emptyList(),
    ) : MessagesState
}