package com.blummock.chattdd.chat_feature.domain.entity

sealed interface MessagesState {

    val messages: List<Message>

    data class Data(
        override val messages: List<Message>
    ) : MessagesState

    data class Error(
        val reason: DomainError,
        override val messages: List<Message> = emptyList(),
    ) : MessagesState
}