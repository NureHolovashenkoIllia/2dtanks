package ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.multiplayer.creategame

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.multiplayer.GameType

@Composable
fun CreateGameScreen(
    currentPlayerId: String,
    onRoomCreated: (String, String, Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: CreateGameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Create Game") },
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
            Spacer(modifier = Modifier.height(16.dp))

            val gameTypes = listOf("Free" to GameType.FREE, "Tournament" to GameType.TOURNAMENT)
            val selectedType = uiState.type

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Game Mode:")
                Spacer(modifier = Modifier.width(12.dp))
                gameTypes.forEach { (label, type) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        RadioButton(
                            selected = selectedType == type,
                            onClick = { viewModel.onGameTypeChange(type) },
                            enabled = !uiState.isCreating
                        )
                        Text(label)
                    }
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.type == GameType.TOURNAMENT) {
                OutlinedTextField(
                    value = uiState.teamsCount,
                    onValueChange = viewModel::onTeamsCountChange,
                    label = { Text("Teams count") },
                    enabled = !uiState.isCreating,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.playersPerTeam,
                    onValueChange = viewModel::onPlayersPerTeamChange,
                    label = { Text("Players per team") },
                    enabled = !uiState.isCreating,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                OutlinedTextField(
                    value = uiState.playersCount,
                    onValueChange = viewModel::onPlayersCountChange,
                    label = { Text("Players count") },
                    enabled = !uiState.isCreating,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.gameDuration,
                onValueChange = viewModel::onGameDurationChange,
                label = { Text("Game duration (min)") },
                enabled = !uiState.isCreating,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.isCreating) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Creating your room...")
                }
            } else {
                Button(
                    onClick = { viewModel.createRoom(currentPlayerId, onRoomCreated) },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Create room")
                }
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
    }
}
