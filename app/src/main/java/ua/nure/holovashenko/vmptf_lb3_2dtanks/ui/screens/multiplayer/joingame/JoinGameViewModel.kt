package ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.multiplayer.joingame

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.multiplayer.GameType

class JoinGameViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(JoinGameUiState())
    val uiState: StateFlow<JoinGameUiState> = _uiState

    private val firestore = FirebaseFirestore.getInstance()

    fun onRoomCodeChange(newCode: String) {
        _uiState.update { it.copy(roomCode = newCode, errorMessage = null) }
    }

    fun onGameTypeChange(type: GameType) {
        _uiState.update { it.copy(gameType = type, errorMessage = null) }
    }

    fun onTeamNameChange(teamName: String) {
        _uiState.update { it.copy(teamName = teamName, errorMessage = null) }
    }

    fun joinRoom(
        playerId: String,
        onSuccess: (String, String, Boolean) -> Unit
    ) {
        val roomCode = _uiState.value.roomCode.trim()
        val selectedType = _uiState.value.gameType

        if (roomCode.isBlank()) {
            showError("Введіть код кімнати")
            return
        }

        _uiState.update { it.copy(isJoining = true, errorMessage = null) }

        val docRef = firestore.collection("gameRooms").document(roomCode)

        docRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                showError("Кімната не знайдена")
                return@addOnSuccessListener
            }

            val roomType = snapshot.getString("type")

            if (roomType == null) {
                showError("Невідомий тип кімнати")
                return@addOnSuccessListener
            }

            if ((roomType == "tournament" && selectedType != GameType.TOURNAMENT) ||
                (roomType == "free" && selectedType != GameType.FREE)
            ) {
                val expected = if (roomType == "tournament") "Tournament" else "Free"
                val actual = if (selectedType == GameType.TOURNAMENT) "Tournament" else "Free"
                showError("Обраний режим ($actual) не відповідає типу кімнати ($expected)")
                return@addOnSuccessListener
            }

            if (roomType == "tournament") {
                joinTournamentRoom(docRef, snapshot.data ?: emptyMap(), playerId, onSuccess)
            } else {
                joinFreeRoom(docRef, snapshot.data ?: emptyMap(), playerId, onSuccess)
            }

        }.addOnFailureListener {
            showError("Помилка при підключенні: ${it.localizedMessage}")
        }
    }

    private fun joinFreeRoom(
        docRef: com.google.firebase.firestore.DocumentReference,
        data: Map<String, Any>,
        playerId: String,
        onSuccess: (String, String, Boolean) -> Unit
    ) {
        val currentPlayers = (data["players"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()

        if (!currentPlayers.contains(playerId)) {
            val updatedPlayers = currentPlayers + playerId
            docRef.update("players", updatedPlayers).addOnSuccessListener {
                onSuccess(docRef.id, playerId, false)
                _uiState.update { it.copy(isJoining = false) }
            }.addOnFailureListener {
                showError("Не вдалося приєднатися")
            }
        } else {
            onSuccess(docRef.id, playerId, false)
            _uiState.update { it.copy(isJoining = false) }
        }
    }

    private fun joinTournamentRoom(
        docRef: com.google.firebase.firestore.DocumentReference,
        data: Map<String, Any>,
        playerId: String,
        onSuccess: (String, String, Boolean) -> Unit
    ) {
        val teamName = _uiState.value.teamName.trim()
        if (teamName.isBlank()) {
            showError("Введіть назву команди"); return
        }

        val playersPerTeam = (data["playersPerTeam"] as? Long)?.toInt() ?: 0
        val teamsMap = (data["teams"] as? Map<*, *>)?.mapNotNull {
            val team = it.key as? String
            val players = (it.value as? List<*>)?.filterIsInstance<String>()
            if (team != null && players != null) team to players else null
        }?.toMap() ?: emptyMap()

        val currentTeam = teamsMap[teamName] ?: run {
            showError("Команду не знайдено"); return
        }

        if (currentTeam.contains(playerId)) {
            onSuccess(docRef.id, playerId, true)
            _uiState.update { it.copy(isJoining = false) }
            return
        }

        if (currentTeam.size >= playersPerTeam) {
            showError("Команда вже заповнена"); return
        }

        val updatedTeams = teamsMap.toMutableMap().apply {
            put(teamName, currentTeam + playerId)
        }

        docRef.update("teams", updatedTeams).addOnSuccessListener {
            onSuccess(docRef.id, playerId, true)
            _uiState.update { it.copy(isJoining = false) }
        }.addOnFailureListener {
            showError("Не вдалося приєднатися")
        }
    }

    private fun showError(message: String) {
        _uiState.update { it.copy(errorMessage = message, isJoining = false) }
    }
}