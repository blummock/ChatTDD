package com.blummock.chattdd.chat_feature.presentation.vm.state

internal sealed interface MessageUiModel {
    val id: String
    val time: String
    val isMine: Boolean
    val status: MessageStatusUi
}

internal data class TextMessageModel(
    override val id: String,
    override val time: String,
    override val isMine: Boolean,
    override val status: MessageStatusUi,
    val text: String,
) : MessageUiModel
