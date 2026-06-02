package com.blummock.chat_feature.presentation.vm

internal sealed interface ChatEffect {

    data class ErrorEffect(val message: String) : ChatEffect
    data object ScrollToBottom : ChatEffect
}