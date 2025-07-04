package ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.multiplayer.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ua.nure.holovashenko.vmptf_lb3_2dtanks.util.SoundPlayer
import java.util.Date

data class Position(val x: Int, val y: Int)
enum class Direction { UP, DOWN, LEFT, RIGHT }
data class Bullet(val ownerId: String, val position: Position, val direction: Direction)
data class Cell(val x: Int, val y: Int, val isObstacle: Boolean)
typealias GameMap = List<List<Cell>>

class GameViewModel(
    application: Application,
    private val roomId: String,
    private val currentPlayerId: String,
    private val repository: GameRepository = GameRepository()
) : AndroidViewModel(application) {

    private var startTime: Long = System.currentTimeMillis()
    private val gridSize = 10

    private val _playerPositions = MutableStateFlow<Map<String, Position>>(emptyMap())
    val playerPositions: StateFlow<Map<String, Position>> = _playerPositions.asStateFlow()

    private val _gameType = MutableStateFlow("free")
    val gameType: StateFlow<String> = _gameType.asStateFlow()

    private val _teams = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val teams: StateFlow<Map<String, List<String>>> = _teams.asStateFlow()

    private val _bullets = MutableStateFlow<List<Bullet>>(emptyList())
    val bullets: StateFlow<List<Bullet>> = _bullets.asStateFlow()

    private val _gameOver = MutableStateFlow(false)
    val gameOver: StateFlow<Boolean> = _gameOver.asStateFlow()

    private val _map = MutableStateFlow<GameMap>(emptyList())
    val map: StateFlow<GameMap> = _map.asStateFlow()

    private val _mapLoaded = MutableStateFlow(false)
    val mapLoaded: StateFlow<Boolean> = _mapLoaded.asStateFlow()

    private val _remainingTime = MutableStateFlow(180)
    val remainingTime: StateFlow<Int> = _remainingTime.asStateFlow()

    private val _playerDirections = MutableStateFlow<Map<String, Direction>>(emptyMap())
    val playerDirections: StateFlow<Map<String, Direction>> = _playerDirections.asStateFlow()

    private val _kills = mutableMapOf<String, Int>()
    private var lastDirection: Direction = Direction.UP
    private var lastWinner: String? = null
    private var playerTeam: String? = null
    private var hasGameEnded = false
    private var wasAlive = true
    private var latestSnapshot: Map<String, Any> = emptyMap()

    private val listener = FirebaseFirestore.getInstance()
        .collection("gameRooms")
        .document(roomId)
        .addSnapshotListener { snapshot, _ ->
            val data = snapshot?.data ?: return@addSnapshotListener
            handleSnapshot(data)
        }

    init {
        viewModelScope.launch {
            fetchGameDuration()
            repository.assignInitialPosition(roomId, currentPlayerId, gridSize)
            ensureMapCreatedOnce()
        }
        startBulletLoop()
    }

    private fun handleSnapshot(data: Map<String, Any>) {
        latestSnapshot = data
        val aliveStatus = (data["aliveStatus"] as? Map<*, *>)?.mapValues { it.value as Boolean } ?: return

        val type = data["type"] as? String ?: "free"
        _gameType.value = type

        val teamMap = data["teams"] as? Map<String, List<String>>
        if (teamMap != null) {
            _teams.value = teamMap
            if (playerTeam == null) {
                playerTeam = teamMap.entries.find { it.value.contains(currentPlayerId) }?.key
            }
        }

        val positionsMap = (data["positions"] as? Map<*, *>)?.mapNotNull { (key, value) ->
            val uid = key as? String ?: return@mapNotNull null
            val pos = value as? Map<*, *> ?: return@mapNotNull null
            val x = (pos["x"] as? Long)?.toInt() ?: return@mapNotNull null
            val y = (pos["y"] as? Long)?.toInt() ?: return@mapNotNull null
            if (aliveStatus[uid] == true) uid to Position(x, y) else null
        }?.toMap() ?: emptyMap()

        if (_playerPositions.value != positionsMap) {
            _playerPositions.value = positionsMap
        }

        val directionMap = (data["directions"] as? Map<*, *>)?.mapNotNull { (key, value) ->
            val uid = key as? String ?: return@mapNotNull null
            val dir = (value as? String)?.let { Direction.valueOf(it) } ?: return@mapNotNull null
            uid to dir
        }?.toMap() ?: emptyMap()

        _playerDirections.value = directionMap

        if (playerTeam == null) {
            val teamMap = data["teams"] as? Map<String, List<String>>
            playerTeam = teamMap?.entries?.find { it.value.contains(currentPlayerId) }?.key
        }

        if (_map.value.isEmpty()) {
            val mapDataRaw = data["map"] as? Map<*, *> ?: emptyMap<String, Boolean>()
            val mapData = List(gridSize) { y ->
                List(gridSize) { x ->
                    val key = "$x,$y"
                    val isObstacle = mapDataRaw[key] as? Boolean ?: false
                    Cell(x, y, isObstacle)
                }
            }
            _map.value = mapData
        }

        val isCurrentlyAlive = aliveStatus[currentPlayerId] == true

        if (!isCurrentlyAlive && wasAlive && !hasGameEnded) {
            val playerKills = _kills[currentPlayerId] ?: 0
            endGame(winnerId = null, kills = playerKills, won = false)
        }

        wasAlive = isCurrentlyAlive
    }

    private suspend fun fetchGameDuration() {
        val snapshot = repository.fetchRoom(roomId) ?: return
        val duration = (snapshot.getLong("gameDuration") ?: 180).toInt()
        _remainingTime.value = duration * 60

        viewModelScope.launch {
            while (_remainingTime.value > 0 && !_gameOver.value) {
                delay(1000)
                _remainingTime.value -= 1
            }
            if (_remainingTime.value == 0) {
                _gameOver.value = true
                endGame(winnerId = "Draw, time is up", kills = _kills[currentPlayerId] ?: 0, won = false)
            }
        }
    }

    fun buildGameResultText(playerId: String): String {
        val winner = lastWinner ?: "Unknown"
        val kills = _kills[currentPlayerId] ?: 0
        val isWinner = winner == playerId || winner == playerTeam

        return buildString {
            appendLine("Game over!")
            appendLine("Winner: $winner")
            appendLine("Your kills: $kills")
            appendLine(if (isWinner) "\n You win!" else "\n You lose.")
        }
    }

    fun move(direction: Direction) {
        viewModelScope.launch {
            val currentPosMap = (latestSnapshot["positions"] as? Map<*, *>)?.get(currentPlayerId) as? Map<*, *> ?: return@launch
            val currentPos = Position((currentPosMap["x"] as Long).toInt(), (currentPosMap["y"] as Long).toInt())

            val newPos = when (direction) {
                Direction.UP -> currentPos.copy(y = (currentPos.y - 1).coerceAtLeast(0))
                Direction.DOWN -> currentPos.copy(y = (currentPos.y + 1).coerceAtMost(gridSize - 1))
                Direction.LEFT -> currentPos.copy(x = (currentPos.x - 1).coerceAtLeast(0))
                Direction.RIGHT -> currentPos.copy(x = (currentPos.x + 1).coerceAtMost(gridSize - 1))
            }

            lastDirection = direction
            _playerDirections.update { it.toMutableMap().apply { put(currentPlayerId, direction) } }
            repository.updatePlayerDirection(roomId, currentPlayerId, direction)

            val mapSnapshot = _map.value
            val targetCell = mapSnapshot.getOrNull(newPos.y)?.getOrNull(newPos.x)
            if (targetCell?.isObstacle == true) return@launch

            val isOccupied = _playerPositions.value.any { (id, pos) ->
                id != currentPlayerId && pos == newPos
            }
            if (isOccupied) return@launch

            repository.updatePlayerPosition(roomId, currentPlayerId, newPos)
        }
    }

    fun shoot() {
        viewModelScope.launch {
            val positionMap = ((latestSnapshot["positions"] as? Map<*, *>)?.get(currentPlayerId)) as? Map<*, *> ?: return@launch
            val shooterPos = Position((positionMap["x"] as Long).toInt(), (positionMap["y"] as Long).toInt())
            val direction = lastDirection

            val newBullet = mapOf(
                "ownerId" to currentPlayerId,
                "position" to mapOf("x" to shooterPos.x, "y" to shooterPos.y),
                "direction" to direction.name
            )

            val currentBullets = latestSnapshot["bullets"] as? List<Map<String, Any?>> ?: emptyList()
            val updatedBullets = currentBullets + newBullet

            repository.updateBullets(roomId, updatedBullets)
        }
    }

    private suspend fun ensureMapCreatedOnce() {
        val snapshot = repository.fetchRoom(roomId) ?: return
        val mapExists = snapshot.contains("map")
        val players = when (latestSnapshot["type"]) {
            "free" -> snapshot.get("players") as? List<*> ?: emptyList<String>()
            "tournament" -> {
                val teamsMap = snapshot.get("teams") as? Map<*, *> ?: emptyMap<String, List<String>>()
                teamsMap.values.flatMap { it as? List<*> ?: emptyList<Any>() }
            }
            else -> emptyList<Any>()
        }
        val isHost = players.firstOrNull() == currentPlayerId

        if (!mapExists && isHost) {
            repository.updateMap(roomId, gridSize)
        }

        repeat(10) {
            val fresh = repository.fetchRoom(roomId)
            val mapDataRaw = fresh?.get("map") as? Map<*, *>
            if (mapDataRaw != null && mapDataRaw.isNotEmpty()) {
                val mapData = List(gridSize) { y ->
                    List(gridSize) { x ->
                        val key = "$x,$y"
                        val isObstacle = mapDataRaw[key] as? Boolean ?: false
                        Cell(x, y, isObstacle)
                    }
                }
                _map.value = mapData
                _mapLoaded.value = true
                return
            }
            delay(200)
        }
    }

    private fun startBulletLoop() {
        viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                updateBullets()
                delay(300L)
            }
        }
    }

    private fun updateBullets() {
        FirebaseFirestore.getInstance().collection("gameRooms").document(roomId).get()
            .addOnSuccessListener { snapshot ->
                val data = snapshot.data ?: return@addOnSuccessListener
                val bullets = (data["bullets"] as? List<Map<String, Any?>>)?.mapNotNull { bulletMap ->
                    val ownerId = bulletMap["ownerId"] as? String ?: return@mapNotNull null
                    val posMap = bulletMap["position"] as? Map<*, *> ?: return@mapNotNull null
                    val dirStr = bulletMap["direction"] as? String ?: return@mapNotNull null
                    Bullet(ownerId, Position((posMap["x"] as Long).toInt(), (posMap["y"] as Long).toInt()), Direction.valueOf(dirStr))
                } ?: return@addOnSuccessListener

                val positionsMap = data["positions"] as? Map<*, *> ?: return@addOnSuccessListener
                val aliveStatus = data["aliveStatus"] as? Map<String, Boolean> ?: return@addOnSuccessListener
                val type = data["type"] as? String ?: "free"
                val teams = if (type == "tournament") {
                    data["teams"] as? Map<String, List<String>> ?: emptyMap()
                } else emptyMap()

                val mapDataRaw = data["map"] as? Map<String, Boolean> ?: emptyMap()

                val updatedBullets = bullets.mapNotNull { bullet ->
                    val newPos = when (bullet.direction) {
                        Direction.UP -> bullet.position.copy(y = bullet.position.y - 1)
                        Direction.DOWN -> bullet.position.copy(y = bullet.position.y + 1)
                        Direction.LEFT -> bullet.position.copy(x = bullet.position.x - 1)
                        Direction.RIGHT -> bullet.position.copy(x = bullet.position.x + 1)
                    }
                    if (newPos.x !in 0 until gridSize || newPos.y !in 0 until gridSize) return@mapNotNull null

                    // Видалити кулю, якщо вона потрапляє в перешкоду
                    val key = "${newPos.x},${newPos.y}"
                    if (mapDataRaw[key] == true) return@mapNotNull null

                    val hit = positionsMap.entries.find { (key, value) ->
                        val uid = key as? String ?: return@find false
                        val pos = value as? Map<*, *> ?: return@find false
                        val posObj = Position((pos["x"] as Long).toInt(), (pos["y"] as Long).toInt())

                        val isAlive = aliveStatus[uid] == true
                        if (!isAlive || posObj != newPos) return@find false

                        if (type == "tournament") {
                            val shooterTeam = teams.entries.find { it.value.contains(bullet.ownerId) }?.key
                            val targetTeam = teams.entries.find { it.value.contains(uid) }?.key
                            shooterTeam != null && shooterTeam != targetTeam
                        } else {
                            true // у "free" режимі всі можуть бити всіх
                        }
                    }
                    if (hit != null) {
                        val victimId = hit.key.toString()
                        val killerId = bullet.ownerId
                        registerKill(killerId, victimId)
                        return@mapNotNull null
                    }
                    bullet.copy(position = newPos)
                }

                if (_bullets.value != updatedBullets) {
                    _bullets.value = updatedBullets
                }

                val firestoreBullets = updatedBullets.map {
                    mapOf(
                        "ownerId" to it.ownerId,
                        "position" to mapOf("x" to it.position.x, "y" to it.position.y),
                        "direction" to it.direction.name
                    )
                }
                viewModelScope.launch {
                    repository.updateBullets(roomId, firestoreBullets)
                }
            }
    }

    private fun registerKill(killerId: String, victimId: String) {
        viewModelScope.launch {
            val appContext = getApplication<Application>()
            SoundPlayer.playRandomHitSound(appContext)

            val snapshot = repository.fetchRoom(roomId) ?: return@launch
            val data = snapshot.data ?: return@launch
            val aliveStatus = (data["aliveStatus"] as? Map<String, Boolean>)?.toMutableMap() ?: return@launch

            aliveStatus[victimId] = false
            repository.updateAliveStatus(roomId, aliveStatus)

            _kills[killerId] = (_kills[killerId] ?: 0) + 1

            val type = data["type"] as? String ?: "free"
            if (type == "tournament") {
                val teams = data["teams"] as? Map<String, List<String>> ?: return@launch

                val teamAliveCounts = teams.mapValues { (_, players) ->
                    players.count { aliveStatus[it] == true }
                }

                val aliveTeams = teamAliveCounts.filter { it.value > 0 }

                if (aliveTeams.size == 1) {
                    val winningTeam = aliveTeams.keys.first()
                    val playerKills = _kills[currentPlayerId] ?: 0
                    endGame(
                        winnerId = winningTeam,
                        kills = playerKills,
                        won = teams[winningTeam]?.contains(currentPlayerId) == true
                    )
                }
            } else {
                val remainingAlive = aliveStatus.filterValues { it }.keys
                if (remainingAlive.size == 1 && remainingAlive.contains(currentPlayerId)) {
                    endGame(currentPlayerId, kills = _kills[currentPlayerId] ?: 0, won = true)
                } else if (remainingAlive.size <= 1 && !remainingAlive.contains(currentPlayerId)) {
                    val winnerId = remainingAlive.firstOrNull()
                    endGame(winnerId = winnerId, kills = _kills[currentPlayerId] ?: 0, won = false)
                }
            }
        }
    }

    private fun endGame(winnerId: String?, kills: Int, won: Boolean) {
        if (hasGameEnded) return
        hasGameEnded = true

        viewModelScope.launch {
            val snapshot = repository.fetchRoom(roomId) ?: return@launch
            val data = snapshot.data ?: return@launch
            val aliveStatus = (data["aliveStatus"] as? Map<String, Boolean>) ?: emptyMap()

            val alivePlayers = aliveStatus.filterValues { it }.keys
            val isLastAlive = alivePlayers.size == 1 && alivePlayers.first() == currentPlayerId

            if (isLastAlive) {
                val type = data["type"] as? String ?: "free"
                val isTournament = type == "tournament"
                val gameId = repository.generateNewGameId()?.toString() ?: System.currentTimeMillis().toString()
                val durationSeconds = ((System.currentTimeMillis() - startTime) / 1000).toInt()

                val historyData = mutableMapOf<String, Any>(
                    "gameId" to gameId,
                    "datetime" to Date(),
                    "durationSeconds" to durationSeconds,
                    "type" to type,
                    "winner" to (winnerId ?: "unknown")
                )

                if (isTournament) {
                    val teams = data["teams"] as? Map<String, List<String>> ?: emptyMap()
                    historyData["teams"] = teams
                } else {
                    val players = data["players"] as? List<String> ?: emptyList()
                    historyData["players"] = players
                }

                repository.saveGameHistory(gameId, historyData)
            }

            repository.updateMatchStats(currentPlayerId, won, kills)
            repository.resetGameState(roomId)

            lastWinner = winnerId
            _gameOver.value = true
        }
    }

    override fun onCleared() {
        super.onCleared()
        listener.remove()
    }
}
