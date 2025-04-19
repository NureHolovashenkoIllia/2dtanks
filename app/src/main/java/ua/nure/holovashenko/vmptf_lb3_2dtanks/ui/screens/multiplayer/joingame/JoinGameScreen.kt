package ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.multiplayer.joingame

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.multiplayer.GameType

@Composable
fun JoinGameScreen(
    currentPlayerId: String,
    onJoinSuccess: (String, String, Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: JoinGameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Join Game") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Choose a game mode", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(8.dp))

            Row {
                RadioButton(
                    selected = uiState.gameType == GameType.FREE,
                    onClick = { viewModel.onGameTypeChange(GameType.FREE) }
                )
                Text("Free", modifier = Modifier.padding(end = 16.dp))

                RadioButton(
                    selected = uiState.gameType == GameType.TOURNAMENT,
                    onClick = { viewModel.onGameTypeChange(GameType.TOURNAMENT) }
                )
                Text("Tournament")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.roomCode,
                onValueChange = { viewModel.onRoomCodeChange(it) },
                label = { Text("Room code") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.gameType == GameType.TOURNAMENT) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.teamName,
                    onValueChange = { viewModel.onTeamNameChange(it) },
                    label = { Text("Team name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.joinRoom(currentPlayerId, onJoinSuccess)
                },
                enabled = uiState.roomCode.isNotBlank() && !uiState.isJoining,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (uiState.isJoining) "Joining..." else "Join")
            }

            uiState.errorMessage?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
