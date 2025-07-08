package ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.multiplayer.creategame

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.multiplayer.GameType
import ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.multiplayer.joingame.SegmentedButtonRow
import ua.nure.holovashenko.vmptf_lb3_2dtanks.R

@Composable
fun CreateGameScreen(
    currentPlayerId: String,
    onRoomCreated: (String, String, Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: CreateGameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val createGameStrings = rememberCreateGameStrings()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.create_game), fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            SegmentedButtonRow(
                options = listOf(GameType.FREE, GameType.TOURNAMENT),
                selected = uiState.type,
                onOptionSelected = viewModel::onGameTypeChange
            )

            Spacer(Modifier.height(16.dp))

            if (uiState.type == GameType.TOURNAMENT) {
                OutlinedTextField(
                    value = uiState.teamsCount,
                    onValueChange = viewModel::onTeamsCountChange,
                    label = { Text(stringResource(R.string.teams_count)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.errorMessage == createGameStrings.errorTeamsCount
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.playersPerTeam,
                    onValueChange = viewModel::onPlayersPerTeamChange,
                    label = { Text(stringResource(R.string.players_per_team)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.errorMessage == createGameStrings.errorPlayersPerTeam
                )
            } else {
                OutlinedTextField(
                    value = uiState.playersCount,
                    onValueChange = viewModel::onPlayersCountChange,
                    label = { Text(stringResource(R.string.players_count)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.errorMessage == createGameStrings.errorPlayersCount
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.gameDuration,
                onValueChange = viewModel::onGameDurationChange,
                label = { Text(stringResource(R.string.game_duration_min)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.errorMessage == createGameStrings.errorGameDuration
            )

            uiState.errorMessage?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.createRoom(currentPlayerId, onRoomCreated, createGameStrings) },
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isCreating
            ) {
                if (uiState.isCreating) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(stringResource(R.string.creating), style = MaterialTheme.typography.bodyLarge)
                } else {
                    Text(stringResource(R.string.create_room), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
fun rememberCreateGameStrings(): CreateGameStrings {
    return CreateGameStrings(
        errorTeamsCount = stringResource(R.string.create_error_teams_count),
        errorPlayersPerTeam = stringResource(R.string.create_error_players_per_team),
        errorPlayersCount = stringResource(R.string.create_error_players_count),
        errorGameDuration = stringResource(R.string.create_error_game_duration),
        errorCreationFailed = stringResource(R.string.create_error_failed),
        errorTimeout = stringResource(R.string.create_error_timeout)
    )
}