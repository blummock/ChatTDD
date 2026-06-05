package com.blummock.chattdd.chat_feature.data

import com.blummock.chattdd.chat_feature.data.mappers.ExceptionMapper
import com.blummock.chattdd.chat_feature.domain.entity.DomainError
import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.io.IOException

class ExceptionMapperTests {

    @Test
    fun scenario() {
        val exceptionMapper = ExceptionMapper()
        assertEquals(DomainError.UnknownError, exceptionMapper.map(RuntimeException()))
        assertEquals(DomainError.NoInternet, exceptionMapper.map(IOException()))
    }
}