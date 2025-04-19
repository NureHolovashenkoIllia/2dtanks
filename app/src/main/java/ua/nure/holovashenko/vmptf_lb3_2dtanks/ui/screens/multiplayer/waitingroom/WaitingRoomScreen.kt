package ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.multiplayer.waitingroom

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@Composable
fun WaitingRoomScreen(
    roomId: String,
    currentPlayerId: String,
    isCreator: Boolean,
    fromGame: Boolean = false,
    onNavigateToMain: () -> Unit,
    onStartGame: () -> Unit,
    viewModel: WaitingRoomViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Спостерігати за кімнатою тільки якщо не повернулись з гри
    if (!fromGame) {
        LaunchedEffect(roomId) {
            viewModel.observeRoom(
                roomId = roomId,
                onRoomClosed = { onNavigateToMain() }
            )
        }
    } else {
        // Навіть якщо повернулись з гри, перевірити чи нас ще не видалили з кімнати
        LaunchedEffect(roomId) {
            viewModel.observeRoomMinimal(roomId) {
                onNavigateToMain()
            }
        }
    }

    LaunchedEffect(uiState.gameStarted) {
        if (uiState.gameStarted && !fromGame) {
            onStartGame()
        }
    }

    LaunchedEffect(isCreator, roomId) {
        if (!isCreator) {
            viewModel.addPlayerIfNeeded(roomId, currentPlayerId)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(roomId))
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Room ID copied")
                            }
                        }
                    ) {
                        Text(roomId, style = MaterialTheme.typography.displaySmall)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.leaveRoom(
                            roomId = roomId,
                            playerId = currentPlayerId,
                            onSuccess = {
                                viewModel.removeListener()
                                onNavigateToMain()
                            },
                            onError = { /* TODO */ }
                        )
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Exit")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text("Waiting for players...", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (uiState.roomType == "tournament") {
                    // Відображаємо навіть пусті команди
                    val allTeams = uiState.teamPlayers.keys.union(uiState.teamEmails.keys)

                    allTeams.forEach { teamName ->
                        val emails = uiState.teamEmails[teamName].orEmpty()

                        item {
                            Text(teamName, style = MaterialTheme.typography.titleMedium)
                        }

                        if (emails.isNotEmpty()) {
                            items(emails) { email ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Text(
                                        text = email,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        } else {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Text(
                                        text = "The command is empty",
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }

                        item { Spacer(Modifier.height(8.dp)) }
                    }
                } else {
                    items(uiState.playerEmails) { email ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(
                                text = email,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }

            uiState.errorMessage?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            if (uiState.playerIds.firstOrNull() == currentPlayerId && !uiState.gameStarted) {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.startGame(roomId) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start Game")
                }
            }
        }
    }
}