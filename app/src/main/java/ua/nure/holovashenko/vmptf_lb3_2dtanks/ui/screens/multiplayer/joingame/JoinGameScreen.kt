package ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.multiplayer.joingame

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.multiplayer.GameType
import ua.nure.holovashenko.vmptf_lb3_2dtanks.R

@Composable
fun JoinGameScreen(
    currentPlayerId: String,
    onJoinSuccess: (String, String, Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: JoinGameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val joinGameStrings = rememberJoinGameStrings()

    val error = uiState.errorMessage?.let { msg ->
        if (msg.startsWith("ROOM_TYPE_MISMATCH:")) {
            val parts = msg.split(":")
            val expected = parts.getOrNull(1) ?: stringResource(R.string.not_available)
            val actual = parts.getOrNull(2) ?: stringResource(R.string.not_available)

            stringResource(R.string.join_error_room_type_mismatch, actual, expected)
        } else msg
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.join_game), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
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
                    label = { Text(stringResource(R.string.room_code)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (uiState.gameType == GameType.TOURNAMENT) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = uiState.teamName,
                        onValueChange = { viewModel.onTeamNameChange(it) },
                        label = { Text(stringResource(R.string.team_name)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                error?.let {
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
                    viewModel.joinRoom(currentPlayerId, onJoinSuccess, joinGameStrings)
                },
                enabled = !uiState.isJoining,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (uiState.isJoining) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(stringResource(R.string.joining), style = MaterialTheme.typography.bodyLarge)
                } else {
                    Text(stringResource(R.string.join), style = MaterialTheme.typography.bodyLarge)
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

@Composable
fun rememberJoinGameStrings(): JoinGameStrings {
    return JoinGameStrings(
        emptyRoomCode = stringResource(R.string.join_error_empty_code),
        roomNotFound = stringResource(R.string.join_error_room_not_found),
        unknownRoomType = stringResource(R.string.join_error_unknown_room_type),
        emptyTeamName = stringResource(R.string.join_error_empty_team_name),
        teamNotFound = stringResource(R.string.join_error_team_not_found),
        teamFull = stringResource(R.string.join_error_team_full),
        joinError = stringResource(R.string.join_error_joining),
        joinFailed = stringResource(R.string.join_error_join_failed)
    )
}