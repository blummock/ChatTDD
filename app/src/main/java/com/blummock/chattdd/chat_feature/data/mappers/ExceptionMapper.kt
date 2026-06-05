package com.blummock.chattdd.chat_feature.data.mappers

import com.blummock.chattdd.chat_feature.domain.entity.DomainError
import java.io.IOException

class ExceptionMapper {

    fun map(exception: Throwable): DomainError = when (exception) {
        is IOException -> DomainError.NoInternet
        else -> DomainError.UnknownError
    }
}