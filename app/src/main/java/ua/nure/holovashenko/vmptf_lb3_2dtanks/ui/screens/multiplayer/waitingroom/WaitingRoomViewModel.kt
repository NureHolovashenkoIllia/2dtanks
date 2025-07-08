package ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.multiplayer.waitingroom

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class WaitingRoomViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private var listenerRegistration: ListenerRegistration? = null

    private val _uiState = MutableStateFlow(WaitingRoomUiState())
    val uiState: StateFlow<WaitingRoomUiState> = _uiState

    private lateinit var strings: WaitingRoomStrings

    fun updateStrings(strings: WaitingRoomStrings) {
        this.strings = strings
    }

    fun observeRoom(roomId: String, onRoomClosed: () -> Unit) {
        removeListener()
        val docRef = firestore.collection("gameRooms").document(roomId)

        listenerRegistration = docRef.addSnapshotListener { snapshot, _ ->
            if (snapshot == null || !snapshot.exists()) {
                onRoomClosed()
                return@addSnapshotListener
            }

            val type = snapshot.getString("type") ?: "free"
            val gameStarted = snapshot.getBoolean("gameStarted") == true
            _uiState.value = _uiState.value.copy(gameStarted = gameStarted)

            if (type == "tournament") {
                handleTournamentRoom(snapshot)
            } else {
                handleFreeRoom(snapshot, onRoomClosed)
            }
        }
    }

    private fun handleTournamentRoom(snapshot: DocumentSnapshot) {
        val teamsRaw = snapshot.get("teams") as? Map<*, *> ?: return
        val teamMap = teamsRaw.mapNotNull { (key, value) ->
            val teamName = key?.toString()
            val players = (value as? List<*>)?.filterIsInstance<String>()
            if (teamName != null && players != null) teamName to players else null
        }.toMap()

        val playerIds = teamMap.values.flatten().distinct()
        val playersPerTeam = (snapshot.get("playersPerTeam") as? Long)?.toInt() ?: 0
        val teamsCount = (snapshot.get("teamsCount") as? Long)?.toInt() ?: 0

        _uiState.value = _uiState.value.copy(
            roomType = "tournament",
            teamPlayers = teamMap,
            playerIds = playerIds,
            playersPerTeam = playersPerTeam,
            teamsCount = teamsCount
        )

        if (playerIds.isNotEmpty()) {
            fetchPlayerEmails(playerIds) { emailMap ->
                val teamEmails = teamMap.mapValues { entry ->
                    entry.value.map { playerId -> emailMap[playerId] ?: strings.unknownPlayer }
                }
                _uiState.value = _uiState.value.copy(teamEmails = teamEmails)
            }
        }
    }

    private fun handleFreeRoom(snapshot: DocumentSnapshot, onRoomClosed: () -> Unit) {
        val playerIds = (snapshot.get("players") as? List<*>)?.filterIsInstance<String>().orEmpty()
        val playersCount = (snapshot.get("playersCount") as? Long)?.toInt() ?: 0

        _uiState.value = _uiState.value.copy(
            roomType = "free",
            playerIds = playerIds,
            playersCount = playersCount
        )

        if (playerIds.isEmpty()) {
            snapshot.reference.delete()
            onRoomClosed()
        } else {
            fetchPlayerEmails(playerIds) { emailMap ->
                val emails = playerIds.map { emailMap[it] ?: strings.unknownPlayer }
                _uiState.value = _uiState.value.copy(playerEmails = emails)
            }
        }
    }

    private fun fetchPlayerEmails(playerIds: List<String>, onResult: (Map<String, String>) -> Unit) {
        firestore.collection("users")
            .whereIn(FieldPath.documentId(), playerIds)
            .get()
            .addOnSuccessListener { result ->
                val emailMap = result.documents.associate {
                    it.id to (it.getString("nickname") ?: strings.unknownPlayer)
                }
                onResult(emailMap)
            }
    }

    fun observeRoomMinimal(roomId: String, onRoomClosed: () -> Unit) {
        removeListener()
        listenerRegistration = firestore.collection("gameRooms")
            .document(roomId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null || !snapshot.exists()) onRoomClosed()
            }
    }

    fun addPlayerIfNeeded(roomId: String, playerId: String) {
        firestore.collection("gameRooms").document(roomId)
            .get()
            .addOnSuccessListener { snapshot ->
                val players = (snapshot.get("players") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                if (!players.contains(playerId)) {
                    snapshot.reference.update("players", players + playerId)
                }
            }
    }

    fun startGame(roomId: String) {
        val roomRef = firestore.collection("gameRooms").document(roomId)
        val aliveMap = _uiState.value.playerIds.associateWith { true }

        roomRef.get().addOnSuccessListener { snapshot ->
            when (snapshot.getString("type")) {
                "free" -> {
                    val players = (snapshot.get("players") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    if (players.size < 2) {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = strings.errorMinPlayers
                        )
                        return@addOnSuccessListener
                    }
                }

                "tournament" -> {
                    val teams = snapshot.get("teams") as? Map<*, *> ?: emptyMap<Any?, Any?>()
                    val anyTeamEmpty = teams.any { (_, value) ->
                        (value as? List<*>)?.filterIsInstance<String>().isNullOrEmpty()
                    }
                    if (anyTeamEmpty) {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = strings.errorTeamEmpty
                        )
                        return@addOnSuccessListener
                    }
                }
            }

            roomRef.update(
                mapOf(
                    "aliveStatus" to aliveMap,
                    "gameStarted" to true
                )
            )
        }
    }

    fun leaveRoom(
        roomId: String,
        playerId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val roomRef = firestore.collection("gameRooms").document(roomId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(roomRef)
            if (!snapshot.exists()) throw Exception(strings.errorRoomNotFound)

            val players = (snapshot.get("players") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            val updatedPlayers = players - playerId

            if (updatedPlayers.isEmpty()) {
                transaction.delete(roomRef)
            } else {
                transaction.update(roomRef, "players", updatedPlayers)
            }
        }.addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.localizedMessage ?: strings.errorLeaveFailed) }
    }

    fun isRoomFull(): Boolean {
        return when (uiState.value.roomType) {
            "tournament" -> {
                val teamPlayers = uiState.value.teamPlayers
                val expectedTeams = uiState.value.teamsCount
                val expectedPlayers = uiState.value.playersPerTeam

                teamPlayers.size == expectedTeams &&
                        teamPlayers.values.all { it.size >= expectedPlayers }
            }
            "free" -> {
                uiState.value.playerIds.size >= uiState.value.playersCount
            }
            else -> false
        }
    }

    fun removeListener() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    override fun onCleared() {
        super.onCleared()
        removeListener()
    }
}