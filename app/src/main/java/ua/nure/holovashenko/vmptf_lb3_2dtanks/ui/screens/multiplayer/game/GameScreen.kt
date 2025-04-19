package ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.multiplayer.game

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun GameScreen(
    roomId: String,
    currentPlayerId: String,
    onGameEnd: () -> Unit
) {
    val viewModel: GameViewModel = viewModel(factory = GameViewModelFactory(roomId, currentPlayerId))

    val playerPositions by viewModel.playerPositions.collectAsState()
    val gameType by viewModel.gameType.collectAsState()
    val teams by viewModel.teams.collectAsState()
    val teamEmojis = remember(teams) {
        val emojiPool = listOf("🟢", "🔴", "🟡", "🔵", "🟣", "🟤")
        teams.keys.shuffled().zip(emojiPool).toMap()
    }
    val bullets by viewModel.bullets.collectAsState()
    val gameOver by viewModel.gameOver.collectAsState()
    val map by viewModel.map.collectAsState()
    val mapLoaded by viewModel.mapLoaded.collectAsState()
    val remainingTime by viewModel.remainingTime.collectAsState()
    val gridSize = 10

    val minutes = remainingTime / 60
    val seconds = remainingTime % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    var showResultDialog by remember { mutableStateOf(false) }
    var resultText by remember { mutableStateOf("") }

    fun emojiForPlayer(playerId: String): String {
        return if (gameType == "free") {
            if (playerId == currentPlayerId) "🟢" else "🔴"
        } else {
            val team = teams.entries.find { it.value.contains(playerId) }?.key
            teamEmojis[team] ?: "⬜"
        }
    }

    LaunchedEffect(gameOver) {
        if (gameOver) {
            resultText = viewModel.buildGameResultText(currentPlayerId)
            showResultDialog = true
        }
    }

    if (showResultDialog) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {
                TextButton(onClick = {
                    showResultDialog = false
                    onGameEnd()
                }) {
                    Text("Ok")
                }
            },
            title = { Text("Game over") },
            text = { Text(resultText) }
        )
    }

    if (!mapLoaded) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Loading the map...")
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Tank battle")
                        Text(
                            "Time: $formattedTime | Players: ${playerPositions.size}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Ігрове поле
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .weight(1f), // Займає весь простір, що залишився
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))
                for (y in 0 until gridSize) {
                    Row {
                        for (x in 0 until gridSize) {
                            val pos = Position(x, y)
                            val playerHere = playerPositions.entries.find { it.value == pos }
                            val bulletHere = bullets.find { it.position == pos }
                            val isObstacle = map.getOrNull(y)?.getOrNull(x)?.isObstacle == true

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    playerHere != null -> {
                                        Text(
                                            emojiForPlayer(playerHere.key),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }

                                    bulletHere != null -> Text("🔸")
                                    isObstacle -> Text("⬛")
                                    else -> Text("⬜")
                                }
                            }
                        }
                    }
                }
            }

            // Джойстик
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(64.dp))
                    Button(onClick = { viewModel.move(Direction.UP) }) {
                        Text("↑")
                    }
                    Spacer(modifier = Modifier.width(64.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = { viewModel.move(Direction.LEFT) }) {
                        Text("←")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(onClick = { viewModel.move(Direction.DOWN) }) {
                        Text("↓")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(onClick = { viewModel.move(Direction.RIGHT) }) {
                        Text("→")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.shoot() }) {
                    Text("Shoot")
                }
            }
        }
    }
}

