package com.blummock.chat_feature.presentation.vm.state

internal data class ChatState(
    val messagesUiState: MessagesUiState,
    val messageInput: String,
    val sendButtonEnabled: Boolean
)
