package com.blummock.chat_feature.domain.entity

sealed class DomainError(val message: String) {
    data object NoInternet : DomainError("Internet connection lost")
    data object UnknownError : DomainError("Something went wrong")
}