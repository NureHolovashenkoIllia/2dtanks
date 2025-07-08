package ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.multiplayer.game

import android.app.Application
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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

    val bullets by viewModel.bullets.collectAsState()
    val gameOver by viewModel.gameOver.collectAsState()
    val map by viewModel.map.collectAsState()
    val mapLoaded by viewModel.mapLoaded.collectAsState()
    val playerDirections by viewModel.playerDirections.collectAsState()
    val remainingTime by viewModel.remainingTime.collectAsState()
    val formattedTime by remember(remainingTime) {
        derivedStateOf {
            val minutes = remainingTime / 60
            val seconds = remainingTime % 60
            "%02d:%02d".format(minutes, seconds)
        }
    }

    val gridSize = 10
    var showResultDialog by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf(GameResult(0, ResultType.LOSE)) }

    LaunchedEffect(gameOver) {
        if (gameOver) {
            result = viewModel.buildGameResult(currentPlayerId)
            showResultDialog = true
        }
    }

    if (showResultDialog) {
        GameResultDialog(
            kills = result.kills,
            resultType = result.result,
            onDismiss = {
                showResultDialog = false
                onGameEnd()
            }
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
                Text(stringResource(R.string.loading_map))
            }
        }
        return
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.game_title), fontWeight = FontWeight.Bold) },
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
                .padding(start = 24.dp, end = 24.dp, top = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            GameStatusBar(
                formattedTime = formattedTime,
                playerCount = playerPositions.size
            )

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
                        contentDescription = stringResource(R.string.up),
                        onClick = { viewModel.move(Direction.UP) }
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        DirectionButton(
                            icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = stringResource(R.string.left),
                            onClick = { viewModel.move(Direction.LEFT) }
                        )

                        Spacer(modifier = Modifier.width(48.dp))

                        DirectionButton(
                            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = stringResource(R.string.right),
                            onClick = { viewModel.move(Direction.RIGHT) }
                        )
                    }

                    DirectionButton(
                        icon = Icons.Filled.KeyboardArrowDown,
                        contentDescription = stringResource(R.string.down),
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
                            contentDescription = stringResource(R.string.desc_shoot),
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
fun GameResultDialog(
    kills: Int,
    resultType: ResultType,
    onDismiss: () -> Unit
) {
    val iconRes = when (resultType) {
        ResultType.WIN -> R.drawable.ic_victory
        ResultType.LOSE -> R.drawable.ic_defeat
        ResultType.DRAW -> R.drawable.ic_draw
    }

    val title = when (resultType) {
        ResultType.WIN -> stringResource(R.string.victory)
        ResultType.LOSE -> stringResource(R.string.defeat)
        ResultType.DRAW -> stringResource(R.string.draw)
    }

    val titleColor = when (resultType) {
        ResultType.WIN -> MaterialTheme.colorScheme.primary
        ResultType.LOSE -> MaterialTheme.colorScheme.error
        ResultType.DRAW -> MaterialTheme.colorScheme.tertiary
    }

    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.button_continue))
            }
        },
        icon = {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier
                    .size(64.dp)
                    .padding(4.dp)
            )
        },
        title = {
            Text(
                text = title,
                color = titleColor,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold)
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.your_kills, kills),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.tap_continue),
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        },
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 8.dp,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
    )
}

@Composable
fun GameStatusBar(
    formattedTime: String,
    playerCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Таймер
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 2.dp,
            shadowElevation = 4.dp,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_timer),
                    contentDescription = stringResource(R.string.timer),
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        // Гравці
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 2.dp,
            shadowElevation = 4.dp,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_players),
                    contentDescription = stringResource(R.string.players),
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.game_players_count, playerCount),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
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
                    painter = painterResource(id = R.drawable.ic_tank),
                    contentDescription = stringResource(R.string.desc_tank),
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
                    painter = painterResource(id = R.drawable.ic_bullet),
                    contentDescription = stringResource(R.string.desc_bullet),
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