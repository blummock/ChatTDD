package com.blummock.chat_feature.presentation.vm.state

internal sealed interface MessageUiModel {
    val id: String
    val timestamp: String
    val status: MessageStatusUi
}

internal data class TextMessageModel(
    override val id: String,
    override val timestamp: String,
    override val status: MessageStatusUi,
    val text: String,
) : MessageUiModel
