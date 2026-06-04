package com.blummock.chattdd.chat_feature.domain.entity

sealed interface ChatResult<out T> {

    data class Success<T>(
        val data: T
    ) : ChatResult<T>

    data class Error(
        val error: DomainError
    ) : ChatResult<Nothing>
}