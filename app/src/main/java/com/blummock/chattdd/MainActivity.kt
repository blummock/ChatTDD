package com.blummock.chattdd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.blummock.chattdd.chat_feature.core.TimeConverter
import com.blummock.chattdd.chat_feature.presentation.ui.ChatScreen
import com.blummock.chattdd.chat_feature.presentation.vm.ChatViewModel
import com.blummock.chattdd.chat_feature.presentation.vm.state.UiMapper
import com.blummock.chattdd.ui.theme.ChatTDDTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ChatViewModel by viewModels {
        viewModelFactory {
            initializer {
                val provider = (application as App).useCasesProvider
                ChatViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    observeMessagesUseCase = provider.observeMessagesUseCase(),
                    sendMessageUseCase = provider.sendMessageUseCase(),
                    uiMapper = UiMapper(TimeConverter.Base())
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChatTDDTheme {
                ChatScreen(viewModel)
            }
        }
    }
}