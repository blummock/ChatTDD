package com.blummock.chattdd.chat_feature.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.blummock.chattdd.chat_feature.presentation.vm.ChatEffect
import com.blummock.chattdd.chat_feature.presentation.vm.ChatViewModel
import com.blummock.chattdd.chat_feature.presentation.vm.state.ChatState
import com.blummock.chattdd.chat_feature.presentation.vm.state.MessageStatusUi
import com.blummock.chattdd.chat_feature.presentation.vm.state.MessageUiModel
import com.blummock.chattdd.chat_feature.presentation.vm.state.MessagesUiState
import com.blummock.chattdd.chat_feature.presentation.vm.state.TextMessageModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChatScreen(viewModel: ChatViewModel) {

    val snackbarHostState = remember { SnackbarHostState() }

    var isScrollingToBottom by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    LaunchedEffect(viewModel.effect) {
        withContext(Dispatchers.Main.immediate) {
            viewModel.effect.collect {
                when (it) {
                    is ChatEffect.ErrorEffect -> snackbarHostState.showSnackbar(it.message)
                    ChatEffect.ScrollToBottom -> isScrollingToBottom = true
                }
            }
        }
    }

    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(isScrollingToBottom, state.messagesUiState) {
        if (isScrollingToBottom && state.messagesUiState is MessagesUiState.Data) {
            isScrollingToBottom = false
            listState.scrollToItem((state.messagesUiState as MessagesUiState.Data).messages.size)
        }
    }

    ChatScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        listState = listState,
        onTextChanged = viewModel::setMessageInput,
        onSend = viewModel::sendMessage
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatScreenContent(
    state: ChatState,
    snackbarHostState: SnackbarHostState,
    listState: LazyListState,
    onTextChanged: (String) -> Unit,
    onSend: () -> Unit
) {
    Scaffold(
        modifier = Modifier.imePadding(),

        snackbarHost = {
            SnackbarHost(
                modifier = Modifier.testTag("snack"),
                hostState = snackbarHostState
            )
        }

    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                when (val messagesState = state.messagesUiState) {
                    is MessagesUiState.Data -> {
                        ListOfMessages(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("messagesList"),
                            listState,
                            messagesState,
                        )
                    }

                    MessagesUiState.Empty -> {
                        Text(
                            modifier = Modifier.testTag("emptyList"),
                            text = "Your messages are empty",
                            textAlign = TextAlign.Center,
                        )
                    }

                    MessagesUiState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(60.dp)
                                .testTag("loadingList"),
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.height(35.dp)
            ) {
                TextField(
                    value = state.messageInput,
                    onValueChange = onTextChanged,
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .testTag("textInput")
                )
                IconButton(
                    modifier = Modifier.testTag("sendButton"),
                    onClick = onSend,
                    enabled = state.sendButtonEnabled
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        tint = if (state.sendButtonEnabled) Color.DarkGray else Color.LightGray
                    )
                }
            }
        }
    }
}

@Composable
private fun ListOfMessages(modifier: Modifier, lazyListState: LazyListState, state: MessagesUiState.Data) {
    LazyColumn(
        modifier = modifier,
        state = lazyListState,
        verticalArrangement = Arrangement.spacedBy(
            space = 10.dp,
            alignment = Alignment.Bottom
        ),
        contentPadding = PaddingValues(10.dp),
    ) {
        itemsIndexed(state.messages, key = { _, item -> item.id }) { index, message ->
            BubblesFactory(message, index)
        }
    }
}

@Composable
private fun BubblesFactory(messageModel: MessageUiModel, index: Int) {
    when (messageModel) {
        is TextMessageModel -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawRect(if (messageModel.isMine) Color(0xFFAACBE8) else Color(0xFFE8AABE))
                    }
                    .testTag("Element at $index"),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    modifier = Modifier
                        .padding(2.dp)
                        .fillMaxHeight()
                        .weight(1f)
                        .testTag("whoos ${messageModel.isMine}"),
                    text = messageModel.text,
                    fontSize = 18.sp
                )
                Row(
                    modifier = Modifier
                        .fillMaxHeight(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        messageModel.time,
                    )
                    SendStatusView(messageModel.status)
                }
            }
        }
    }
}

@Composable
private fun SendStatusView(status: MessageStatusUi) {

    Row(verticalAlignment = Alignment.CenterVertically) {

        when (status) {
            MessageStatusUi.SENDING -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(14.dp)
                        .testTag("status $status"),
                    strokeWidth = 2.dp
                )
            }

            MessageStatusUi.DELIVERED -> {
                Icon(
                    modifier = Modifier
                        .size(14.dp)
                        .testTag("status $status"),
                    imageVector = Icons.Default.Check,
                    contentDescription = null
                )
            }

            MessageStatusUi.ERROR -> {
                Icon(
                    modifier = Modifier
                        .size(14.dp)
                        .testTag("status $status"),
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color.Red
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun ChatScreenContentPreview() {
    val state = ChatState(
        messagesUiState = MessagesUiState.Data(
            messages = listOf(
                TextMessageModel(
                    id = "dicunt",
                    time = "20:11",
                    isMine = false,
                    status = MessageStatusUi.DELIVERED,
                    text = "tantas ermeprrewew \ndwewe"
                ),
                TextMessageModel(
                    id = "at",
                    time = "20:14",
                    isMine = true,
                    status = MessageStatusUi.SENDING,
                    text = "quaerendum delwew"
                ),
                TextMessageModel(
                    id = "euripidis",
                    time = "20:15",
                    isMine = true,
                    status = MessageStatusUi.ERROR,
                    text = "wisi ewe\nwew ewwe"
                )
            )
        ),
        messageInput = "Hello",
        sendButtonEnabled = true,
    )
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    ChatScreenContent(state, snackbarHostState, listState, {}, {})
}