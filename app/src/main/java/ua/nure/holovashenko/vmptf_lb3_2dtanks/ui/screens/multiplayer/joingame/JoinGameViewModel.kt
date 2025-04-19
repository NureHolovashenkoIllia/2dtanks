package ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.multiplayer.joingame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.multiplayer.GameType

class JoinGameViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(JoinGameUiState())
    val uiState: StateFlow<JoinGameUiState> = _uiState

    private val firestore = FirebaseFirestore.getInstance()

    fun onRoomCodeChange(newCode: String) {
        _uiState.value = _uiState.value.copy(roomCode = newCode, errorMessage = null)
    }

    fun onGameTypeChange(type: GameType) {
        _uiState.value = _uiState.value.copy(gameType = type, errorMessage = null)
    }

    fun onTeamNameChange(teamName: String) {
        _uiState.value = _uiState.value.copy(teamName = teamName, errorMessage = null)
    }

    fun joinRoom(
        playerId: String,
        onSuccess: (String, String, Boolean) -> Unit
    ) {
        val roomCode = _uiState.value.roomCode.trim()
        if (roomCode.isBlank()) return

        _uiState.value = _uiState.value.copy(isJoining = true)

        val docRef = firestore.collection("gameRooms").document(roomCode)

        docRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                _uiState.value = _uiState.value.copy(errorMessage = "Кімната не знайдена", isJoining = false)
                return@addOnSuccessListener
            }

            val roomType = snapshot.getString("type")

            if (roomType == "tournament") {
                val selectedTeam = _uiState.value.teamName.trim()
                if (selectedTeam.isBlank()) {
                    _uiState.value = _uiState.value.copy(errorMessage = "Введіть назву команди", isJoining = false)
                    return@addOnSuccessListener
                }

                val playersPerTeam = (snapshot.get("playersPerTeam") as? Long)?.toInt() ?: 0
                val teamsMap = snapshot.get("teams") as? Map<String, List<String>> ?: emptyMap()
                val currentTeam = teamsMap[selectedTeam] ?: run {
                    _uiState.value = _uiState.value.copy(errorMessage = "Команду не знайдено", isJoining = false)
                    return@addOnSuccessListener
                }

                if (currentTeam.contains(playerId)) {
                    onSuccess(roomCode, playerId, true)
                    _uiState.value = _uiState.value.copy(isJoining = false)
                    return@addOnSuccessListener
                }

                if (currentTeam.size >= playersPerTeam) {
                    _uiState.value = _uiState.value.copy(errorMessage = "Команда вже заповнена", isJoining = false)
                    return@addOnSuccessListener
                }

                val updatedTeam = currentTeam + playerId
                val updatedTeams = teamsMap.toMutableMap()
                updatedTeams[selectedTeam] = updatedTeam

                docRef.update("teams", updatedTeams).addOnSuccessListener {
                    onSuccess(roomCode, playerId, true)
                    _uiState.value = _uiState.value.copy(isJoining = false)
                }.addOnFailureListener {
                    _uiState.value = _uiState.value.copy(errorMessage = "Не вдалося приєднатися", isJoining = false)
                }
            } else {
                val currentPlayers = (snapshot.get("players") as? List<*>)?.filterIsInstance<String>() ?: emptyList()

                if (!currentPlayers.contains(playerId)) {
                    val updatedPlayers = currentPlayers + playerId
                    docRef.update("players", updatedPlayers).addOnSuccessListener {
                        onSuccess(roomCode, playerId, false)
                    }
                } else {
                    onSuccess(roomCode, playerId, false)
                }
                _uiState.value = _uiState.value.copy(isJoining = false)
            }
        }.addOnFailureListener {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Помилка при підключенні",
                isJoining = false
            )
        }
    }
}