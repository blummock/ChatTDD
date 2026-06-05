package com.blummock.chattdd.chat_feature.presentation.vm.state

internal sealed interface MessagesUiState {

    data class Data(
        val messages: List<MessageUiModel>
    ) : MessagesUiState

    data object Loading : MessagesUiState
    data object Empty : MessagesUiState
}