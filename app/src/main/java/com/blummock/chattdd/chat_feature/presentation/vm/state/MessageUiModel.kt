package com.blummock.chattdd.chat_feature.presentation.vm.state

internal sealed interface MessageUiModel {
    val id: String
    val timestamp: Long
    val time: String
    val isMine: String
    val status: MessageStatusUi
}

internal data class TextMessageModel(
    override val id: String,
    override val timestamp: Long,
    override val time: String,
    override val isMine: String,
    override val status: MessageStatusUi,
    val text: String,
) : MessageUiModel
