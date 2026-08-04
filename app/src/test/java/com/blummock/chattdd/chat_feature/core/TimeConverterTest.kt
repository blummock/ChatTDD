package com.blummock.chattdd.chat_feature.core

import kotlinx.datetime.TimeZone
import org.junit.Assert.*
import org.junit.Test

class TimeConverterTest {

    @Test
    fun `scenario with default timezone`() {
        val timeConverter = TimeConverter.Base()
        assertEquals("11:32", timeConverter.toHHmm("1785843165123L"))
    }

    @Test
    fun `scenario with custom timezone`() {
        val timeConverter = TimeConverter.Base(TimeZone.of("+03:00"))
        assertEquals("14:32", timeConverter.toHHmm("1785843165123L"))
    }

}