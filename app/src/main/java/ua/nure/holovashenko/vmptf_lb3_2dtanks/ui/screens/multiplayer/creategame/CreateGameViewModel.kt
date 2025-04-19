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

    fun onPlayersCountChange(value: String) {
        _uiState.value = _uiState.value.copy(playersCount = value)
    }

    fun onGameDurationChange(value: String) {
        _uiState.value = _uiState.value.copy(gameDuration = value)
    }

    fun onTeamsCountChange(value: String) {
        _uiState.value = _uiState.value.copy(teamsCount = value)
    }

    fun onPlayersPerTeamChange(value: String) {
        _uiState.value = _uiState.value.copy(playersPerTeam = value)
    }

    fun onGameTypeChange(type: GameType) {
        _uiState.value = _uiState.value.copy(type = type)
    }

    fun createRoom(currentPlayerId: String, onRoomCreated: (String, String, Boolean) -> Unit) {
        _uiState.value = _uiState.value.copy(isCreating = true, errorMessage = null)

        creationJob = viewModelScope.launch {
            val roomId = generateUniqueRoomId()
            val isTournament = _uiState.value.type == GameType.TOURNAMENT

            val roomData = mutableMapOf<String, Any>(
                "roomId" to roomId,
                "gameDuration" to _uiState.value.gameDuration.toInt(),
                "type" to if (isTournament) "tournament" else "free"
            )

            if (isTournament) {
                val teamsCount = _uiState.value.teamsCount.toInt()
                val playersPerTeam = _uiState.value.playersPerTeam.toInt()
                roomData["teamsCount"] = teamsCount
                roomData["playersPerTeam"] = playersPerTeam

                val teams = mutableMapOf<String, List<String>>()

                // Можна додати currentPlayerId до першої команди:
                teams["team1"] = listOf(currentPlayerId)

                // Інші команди поки порожні
                for (i in 2..teamsCount) {
                    teams["team$i"] = emptyList()
                }

                roomData["teams"] = teams
            } else {
                // Звичайний режим: гравці у списку players
                roomData["playersCount"] = _uiState.value.playersCount.toInt()
                roomData["players"] = listOf(currentPlayerId)
            }

            val job = launch {
                firestore.collection("gameRooms").document(roomId)
                    .set(roomData)
                    .addOnSuccessListener {
                        _uiState.value = _uiState.value.copy(isCreating = false)
                        onRoomCreated(roomId, currentPlayerId, true)
                    }
                    .addOnFailureListener {
                        _uiState.value = _uiState.value.copy(
                            isCreating = false,
                            errorMessage = "Не вдалося створити кімнату. Спробуйте ще раз."
                        )
                    }
            }

            delay(5000L)
            if (job.isActive) {
                job.cancel()
                _uiState.value = _uiState.value.copy(
                    isCreating = false,
                    errorMessage = "Таймаут: сервер не відповідає. Перевірте з'єднання."
                )
            }
        }
    }

    private suspend fun generateUniqueRoomId(): String {
        var roomId: String
        var exists: Boolean
        do {
            roomId = UUID.randomUUID().toString().substring(0, 6)
            val snapshot = firestore.collection("gameRooms").document(roomId).get().await()
            exists = snapshot.exists()
        } while (exists)
        return roomId
    }
}