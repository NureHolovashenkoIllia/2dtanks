package ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.multiplayer.game

import android.app.Application
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ua.nure.holovashenko.vmptf_lb3_2dtanks.R
import ua.nure.holovashenko.vmptf_lb3_2dtanks.util.SoundPlayer
import kotlin.collections.getOrNull

@Composable
fun GameScreen(
    roomId: String,
    currentPlayerId: String,
    onGameEnd: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val viewModel: GameViewModel = viewModel(
        factory = GameViewModelFactory(application, roomId, currentPlayerId)
    )

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
    val playerDirections by viewModel.playerDirections.collectAsState()
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
                .padding(paddingValues)
                .padding(start = 24.dp, end = 24.dp, top = 48.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // Ігрове поле
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (y in 0 until gridSize) {
                    Row(
                        horizontalArrangement = Arrangement.Center
                    ) {
                        for (x in 0 until gridSize) {
                            GameCellContent(
                                position = Position(x, y),
                                playerPositions = playerPositions,
                                playerDirections = playerDirections,
                                bullets = bullets,
                                map = map,
                                currentPlayerId = currentPlayerId,
                                teams = teams,
                                gameType = gameType
                            )
                        }
                    }
                }
            }

            // Джойстик
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.padding(8.dp)
                ) {

                    DirectionButton(
                        icon = Icons.Filled.KeyboardArrowUp,
                        contentDescription = "Up",
                        onClick = { viewModel.move(Direction.UP) }
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        DirectionButton(
                            icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Left",
                            onClick = { viewModel.move(Direction.LEFT) }
                        )

                        Spacer(modifier = Modifier.width(48.dp))

                        DirectionButton(
                            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Right",
                            onClick = { viewModel.move(Direction.RIGHT) }
                        )
                    }

                    DirectionButton(
                        icon = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Down",
                        onClick = { viewModel.move(Direction.DOWN) }
                    )
                }

                Surface(
                    onClick = {
                        viewModel.shoot()
                        SoundPlayer.playShootSound(context)
                    },
                    shape = CircleShape,
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(104.dp)
                        .padding(8.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_fire),
                            contentDescription = "Shoot",
                            modifier = Modifier
                                .size(40.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GameCellContent(
    position: Position,
    playerPositions: Map<String, Position>,
    playerDirections: Map<String, Direction>,
    bullets: List<Bullet>,
    map: List<List<Cell>>,
    currentPlayerId: String,
    teams: Map<String, List<String>>,
    gameType: String
) {
    val playerIdHere = playerPositions.entries.find { it.value == position }?.key
    val bulletHere = bullets.find { it.position == position }
    val isObstacle = map.getOrNull(position.y)?.getOrNull(position.x)?.isObstacle == true

    Box(
        modifier = Modifier
            .size(32.dp)
            .padding(2.dp)
            .background(
                when {
                    isObstacle -> MaterialTheme.colorScheme.outlineVariant
                    else -> MaterialTheme.colorScheme.surfaceContainer
                },
                shape = MaterialTheme.shapes.small
            ),
        contentAlignment = Alignment.Center
    ) {
        when {
            playerIdHere != null -> {
                val direction = playerDirections[playerIdHere] ?: Direction.UP
                val color = getPlayerColor(playerIdHere, currentPlayerId, gameType, teams)

                Image(
                    painter = painterResource(id = R.drawable.tank_base),
                    contentDescription = "Tank",
                    colorFilter = ColorFilter.tint(color = color),
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer { rotationZ = direction.toRotation() }
                )
            }

            bulletHere != null -> {
                val rotation = bulletHere.direction.toRotation()
                val color = getPlayerColor(
                    playerId = bulletHere.ownerId,
                    currentPlayerId = currentPlayerId,
                    gameType = gameType,
                    teams = teams
                )

                Image(
                    painter = painterResource(id = R.drawable.bullet),
                    contentDescription = "Bullet",
                    colorFilter = ColorFilter.tint(color = color),
                    modifier = Modifier
                        .size(12.dp)
                        .graphicsLayer { rotationZ = rotation }
                )
            }
        }
    }
}

@Composable
fun DirectionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        tonalElevation = 4.dp,
        shadowElevation = 6.dp,
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier
            .size(56.dp)
            .padding(4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

fun Direction.toRotation(): Float = when (this) {
    Direction.UP -> 0f
    Direction.RIGHT -> 90f
    Direction.DOWN -> 180f
    Direction.LEFT -> 270f
}

@Composable
fun getPlayerColor(
    playerId: String,
    currentPlayerId: String,
    gameType: String,
    teams: Map<String, List<String>>
): Color {
    return when {
        gameType == "free" && playerId == currentPlayerId -> MaterialTheme.colorScheme.primary
        gameType == "free" -> MaterialTheme.colorScheme.error
        else -> {
            val team = teams.entries.find { it.value.contains(playerId) }?.key
            when (team.hashCode() % 5) {
                0 -> MaterialTheme.colorScheme.primary
                1 -> MaterialTheme.colorScheme.tertiary
                2 -> MaterialTheme.colorScheme.secondary
                3 -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.inversePrimary
            }
        }
    }
}