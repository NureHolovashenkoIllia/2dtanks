package ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.multiplayer.creategame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.multiplayer.GameType
import java.util.UUID

class CreateGameViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CreateGameUiState())
    val uiState: StateFlow<CreateGameUiState> = _uiState

    private val firestore = FirebaseFirestore.getInstance()
    private var creationJob: Job? = null

    fun onPlayersCountChange(value: String) = updateState { it.copy(playersCount = value) }

    fun onGameDurationChange(value: String) = updateState { it.copy(gameDuration = value) }

    fun onTeamsCountChange(value: String) = updateState { it.copy(teamsCount = value) }

    fun onPlayersPerTeamChange(value: String) = updateState { it.copy(playersPerTeam = value) }

    fun onGameTypeChange(type: GameType) = updateState { it.copy(type = type, errorMessage = null) }

    fun createRoom(currentPlayerId: String, onRoomCreated: (String, String, Boolean) -> Unit) {
        if (!validateInputs()) return

        creationJob?.cancel()
        updateState { it.copy(isCreating = true, errorMessage = null) }

        creationJob = viewModelScope.launch {
            val roomId = generateUniqueRoomId()
            val isTournament = uiState.value.type == GameType.TOURNAMENT

            val roomData = buildRoomData(roomId, currentPlayerId, isTournament)

            val firebaseJob = launch {
                firestore.collection("gameRooms").document(roomId)
                    .set(roomData)
                    .addOnSuccessListener {
                        updateState { it.copy(isCreating = false) }
                        onRoomCreated(roomId, currentPlayerId, true)
                    }
                    .addOnFailureListener {
                        updateState {
                            it.copy(
                                isCreating = false,
                                errorMessage = "Не вдалося створити кімнату. Спробуйте ще раз."
                            )
                        }
                    }
            }

            delay(5000L)
            if (firebaseJob.isActive) {
                firebaseJob.cancel()
                updateState {
                    it.copy(
                        isCreating = false,
                        errorMessage = "Таймаут: сервер не відповідає. Перевірте з'єднання."
                    )
                }
            }
        }
    }

    private fun buildRoomData(roomId: String, playerId: String, isTournament: Boolean): Map<String, Any> {
        val state = uiState.value
        return buildMap {
            put("roomId", roomId)
            put("gameDuration", state.gameDuration.toInt())
            put("type", if (isTournament) "tournament" else "free")

            if (isTournament) {
                val teamsCount = state.teamsCount.toInt()
                val playersPerTeam = state.playersPerTeam.toInt()
                put("teamsCount", teamsCount)
                put("playersPerTeam", playersPerTeam)

                val teams = (1..teamsCount).associate { i ->
                    "team$i" to if (i == 1) listOf(playerId) else emptyList()
                }

                put("teams", teams)
            } else {
                put("playersCount", state.playersCount.toInt())
                put("players", listOf(playerId))
            }
        }
    }

    private fun validateInputs(): Boolean {
        val state = uiState.value
        return when (state.type) {
            GameType.TOURNAMENT -> {
                val teams = state.teamsCount.toIntOrNull()
                val perTeam = state.playersPerTeam.toIntOrNull()
                val duration = state.gameDuration.toIntOrNull()

                when {
                    teams == null || teams < 2 -> showError("Teams count must be at least 2")
                    perTeam == null || perTeam < 1 -> showError("Players per team must be at least 1")
                    duration == null || duration < 1 -> showError("Game duration must be at least 1")
                    else -> true
                }
            }

            GameType.FREE -> {
                val count = state.playersCount.toIntOrNull()
                val duration = state.gameDuration.toIntOrNull()

                when {
                    count == null || count < 2 -> showError("Players count must be at least 2")
                    duration == null || duration < 1 -> showError("Game duration must be at least 1")
                    else -> true
                }
            }
        }
    }

    private fun showError(message: String): Boolean {
        updateState { it.copy(errorMessage = message) }
        return false
    }

    private fun updateState(update: (CreateGameUiState) -> CreateGameUiState) {
        _uiState.value = update(_uiState.value)
    }

    private suspend fun generateUniqueRoomId(): String {
        var roomId: String
        var exists: Boolean
        do {
            roomId = UUID.randomUUID().toString().substring(0, 6)
            exists = firestore.collection("gameRooms").document(roomId).get().await().exists()
        } while (exists)
        return roomId
    }
}