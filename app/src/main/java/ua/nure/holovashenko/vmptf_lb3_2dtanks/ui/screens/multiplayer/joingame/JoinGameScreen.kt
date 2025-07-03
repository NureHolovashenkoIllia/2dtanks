package ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.multiplayer.joingame

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
                title = { Text("Join Game", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(24.dp))

                SegmentedButtonRow(
                    options = listOf(GameType.FREE, GameType.TOURNAMENT),
                    selected = uiState.gameType,
                    onOptionSelected = { viewModel.onGameTypeChange(it) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = uiState.roomCode,
                    onValueChange = { viewModel.onRoomCodeChange(it) },
                    label = { Text("Room code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (uiState.gameType == GameType.TOURNAMENT) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = uiState.teamName,
                        onValueChange = { viewModel.onTeamNameChange(it) },
                        label = { Text("Team name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                uiState.errorMessage?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Button(
                onClick = {
                    viewModel.joinRoom(currentPlayerId, onJoinSuccess)
                },
                enabled = !uiState.isJoining,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (uiState.isJoining) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Joining...", style = MaterialTheme.typography.bodyLarge)
                } else {
                    Text("Join", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
fun SegmentedButtonRow(
    options: List<GameType>,
    selected: GameType,
    onOptionSelected: (GameType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = selected == option
            AssistChip(
                onClick = { onOptionSelected(option) },
                label = {
                    Text(
                        when (option) {
                            GameType.FREE -> "Free"
                            GameType.TOURNAMENT -> "Tournament"
                        }
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = null,
                modifier = Modifier.weight(1f)
            )
        }
    }
}