package com.blummock.chattdd.chat_feature.core

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

interface TimeConverter {

    fun toHHmm(millis: Long): String

    class Base(private val timeZone: TimeZone = TimeZone.UTC) : TimeConverter {
        @OptIn(ExperimentalTime::class)
        override fun toHHmm(millis: Long): String {
            val instant = Instant.fromEpochMilliseconds(millis)
            val gmtDateTime = instant.toLocalDateTime(timeZone)
            val hours = gmtDateTime.hour.toString().padStart(2, '0')
            val minutes = gmtDateTime.minute.toString().padStart(2, '0')
            return "$hours:$minutes"
        }
    }
}