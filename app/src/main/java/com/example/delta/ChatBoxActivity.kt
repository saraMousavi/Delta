package com.example.delta

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.delta.data.entity.ChatManagerDto
import com.example.delta.data.entity.ChatMessageDto
import com.example.delta.data.entity.ChatThreadDto
import com.example.delta.init.Preference
import com.example.delta.viewmodel.SharedViewModel
import kotlin.collections.orEmpty

class ChatBoxActivity : ComponentActivity() {

    val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppTheme(useDarkTheme = sharedViewModel.isDarkModeEnabled) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    ChatScreen(
                        sharedViewModel = sharedViewModel,
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    sharedViewModel: SharedViewModel,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val managers by sharedViewModel.chatManagers.collectAsState()
    val chatState by sharedViewModel.currentChat.collectAsState()
    val threads by sharedViewModel.chatThreads.collectAsState()

    var showManagerDialog by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        sharedViewModel.loadChatManagersForCurrentUser()
        sharedViewModel.loadChatThreadsForCurrentUser()
        sharedViewModel.refreshUnreadCount()
    }

    val currentThread = chatState?.thread
    val messages = chatState?.messages.orEmpty()

    LaunchedEffect(currentThread?.threadId) {
        if (currentThread?.threadId != null && currentThread.threadId != 0L) {
            sharedViewModel.markCurrentThreadRead()
            sharedViewModel.startChatPolling(context)
        }
    }

    Scaffold(
        topBar = {
            if (currentThread == null) {
                CenterAlignedTopAppBar(
                    navigationIcon = {
                        IconButton(onClick = {
                            onBack?.invoke() ?: (context as? Activity)?.finish()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null
                            )
                        }
                    },
                    title = {
                        Text(
                            text = stringResource(id = R.string.chat_title),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                )
            } else {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { sharedViewModel.closeCurrentChat() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null
                            )
                        }
                    },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.profilepic),
                                contentDescription = stringResource(id = R.string.profile_image),
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(8.dp))

                            val displayName = when {
                                !chatState?.peerName.isNullOrBlank() -> chatState?.peerName
                                !currentThread.partnerFullName.isNullOrBlank() -> currentThread.partnerFullName
                                !currentThread.partnerFirstName.isNullOrBlank() ||
                                        !currentThread.partnerLastName.isNullOrBlank() ->
                                    listOfNotNull(
                                        currentThread.partnerFirstName,
                                        currentThread.partnerLastName
                                    ).joinToString(" ")
                                else -> stringResource(id = R.string.unknown_person)
                            }

                            Column {
                                Text(
                                    text = displayName ?: "",
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val subtitle = currentThread.partnerSubtitle.orEmpty()
                                if (subtitle.isNotBlank()) {
                                    Text(
                                        text = subtitle,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            if (currentThread == null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.chat_title),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    OutlinedButton(onClick = { showManagerDialog = true }) {
                        Text(
                            text = stringResource(id = R.string.chat_select_manager),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                if (threads.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.chat_start_hint),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(threads, key = { it.threadId }) { thread ->
                            ChatThreadItem(
                                thread = thread,
                                onClick = {
                                    sharedViewModel.openExistingThread(context, thread)
                                }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    ChatMessagesList(messages = messages)
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding(),   // فقط این ردیف با کیبورد بالا بیاید
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        maxLines = 4,
                        textStyle = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val text = inputText.trim()
                            if (text.isNotEmpty()) {
                                sharedViewModel.sendChatMessage(context, text)
                                inputText = ""
                            }
                        },
                        enabled = inputText.isNotBlank()
                    ) {
                        Text(
                            text = stringResource(id = R.string.chat_send),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }

    if (showManagerDialog) {
        ManagersDialog(
            managers = managers,
            onDismiss = { showManagerDialog = false },
            onSelect = { manager ->
                sharedViewModel.openChatWithManager(
                    context = context,
                    managerUserId = manager.userId,
                    managerName = manager.fullName,
                    buildingId = manager.buildingId
                )
                showManagerDialog = false
            }
        )
    }
}

@Composable
private fun ChatMessagesList(messages: List<ChatMessageDto>) {
    val context = LocalContext.current
    val userId = Preference().getUserId(context)

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(messages) { msg ->
            val isMine = msg.senderId == userId
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
            ) {
                Surface(
                    color = if (isMine) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = msg.text,
                        modifier = Modifier.padding(8.dp),
                        color = if (isMine) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}



@Composable
fun ManagersDialog(
    managers: List<ChatManagerDto>,
    onDismiss: () -> Unit,
    onSelect: (ChatManagerDto) -> Unit
) {

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.chat_select_manager),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            if (managers.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.chat_manager_not_found),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(managers) { manager ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(manager) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = stringResource(id = R.string.profile_image),
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = manager.fullName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val subtitle = buildString {
                                    if (!manager.mobileNumber.isNullOrBlank()) {
                                        append(manager.mobileNumber)
                                    }
                                    if (!manager.buildingName.isNullOrBlank()) {
                                        if (isNotEmpty()) append(" • ")
                                        append(manager.buildingName)
                                    }
                                }
                                if (subtitle.isNotBlank()) {
                                    Text(
                                        text = subtitle,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(id = R.string.chat_close),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    )
}
@Composable
fun ChatThreadItem(
    thread: ChatThreadDto,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.profilepic),
            contentDescription = stringResource(id = R.string.profile_image),
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            val name = when {
                !thread.partnerFullName.isNullOrBlank() -> thread.partnerFullName
                !thread.partnerFirstName.isNullOrBlank() ||
                        !thread.partnerLastName.isNullOrBlank() ->
                    listOfNotNull(thread.partnerFirstName, thread.partnerLastName).joinToString(" ")
                else -> stringResource(id = R.string.unknown_person)
            }

            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            val subtitle = buildString {
                if (!thread.partnerMobileNumber.isNullOrBlank()) {
                    append(thread.partnerMobileNumber)
                }
                if (!thread.buildingName.isNullOrBlank()) {
                    if (isNotEmpty()) append(" • ")
                    append(thread.buildingName)
                }
            }
//            if (subtitle.isNotBlank()) {
//                Text(
//                    text = subtitle,
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
//                )
//            }

            val preview = thread.lastMessageText.orEmpty()
            if (preview.isNotBlank()) {
                Text(
                    text = preview,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (thread.unreadCount > 0) {
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = thread.unreadCount.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
            }
        }
    }
}


