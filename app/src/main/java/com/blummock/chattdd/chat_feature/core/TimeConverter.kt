package com.blummock.chattdd.chat_feature.core

interface TimeConverter {

    fun toHHmm(millis: Long): String
}