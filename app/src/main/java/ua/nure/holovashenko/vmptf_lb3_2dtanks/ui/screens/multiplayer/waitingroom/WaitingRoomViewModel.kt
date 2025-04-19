package ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.multiplayer.waitingroom

import androidx.lifecycle.ViewModel
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

    fun observeRoom(roomId: String, onRoomClosed: () -> Unit) {
        removeListener()
        val docRef = firestore.collection("gameRooms").document(roomId)

        listenerRegistration = docRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                val type = snapshot.getString("type") ?: "free"

                if (type == "tournament") {
                    val teams = snapshot.get("teams") as? Map<*, *> ?: emptyMap<Any?, Any?>()
                    val teamMap = mutableMapOf<String, List<String>>()

                    teams.forEach { (teamName, playerList) ->
                        val players = (playerList as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                        teamMap[teamName.toString()] = players
                    }

                    val allPlayerIds = teamMap.values.flatten().distinct()
                    _uiState.value = _uiState.value.copy(
                        playerIds = allPlayerIds,
                        roomType = "tournament",
                        teamPlayers = teamMap
                    )

                    if (allPlayerIds.isNotEmpty()) {
                        firestore.collection("users")
                            .whereIn(FieldPath.documentId(), allPlayerIds)
                            .get()
                            .addOnSuccessListener { result ->
                                val emailMap = result.documents.associateBy({ it.id }, { it.getString("email") ?: "Невідомо" })
                                val teamEmails = teamMap.mapValues { entry ->
                                    entry.value.map { emailMap[it] ?: "Невідомо" }
                                }
                                _uiState.value = _uiState.value.copy(teamEmails = teamEmails)
                            }
                    }
                } else {
                    val uids = snapshot.get("players") as? List<*> ?: emptyList<String>()
                    val playerIds = uids.filterIsInstance<String>()

                    _uiState.value = _uiState.value.copy(playerIds = playerIds, roomType = "free")

                    if (playerIds.isNotEmpty()) {
                        firestore.collection("users")
                            .whereIn(FieldPath.documentId(), playerIds)
                            .get()
                            .addOnSuccessListener { result ->
                                val emails = result.documents.map { it.getString("email") ?: "Невідомо" }
                                _uiState.value = _uiState.value.copy(playerEmails = emails)
                            }
                    } else {
                        docRef.delete()
                        onRoomClosed()
                    }
                }

                val started = snapshot.getBoolean("gameStarted") ?: false
                _uiState.value = _uiState.value.copy(gameStarted = started)
            } else {
                onRoomClosed()
            }
        }
    }

    fun observeRoomMinimal(roomId: String, onRoomClosed: () -> Unit) {
        removeListener()
        val docRef = firestore.collection("gameRooms").document(roomId)

        listenerRegistration = docRef.addSnapshotListener { snapshot, _ ->
            if (snapshot == null || !snapshot.exists()) {
                onRoomClosed()
            }
        }
    }

    fun addPlayerIfNeeded(roomId: String, playerId: String) {
        val docRef = firestore.collection("gameRooms").document(roomId)
        docRef.get().addOnSuccessListener { snapshot ->
            val currentPlayers = (snapshot.get("players") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            if (!currentPlayers.contains(playerId)) {
                docRef.update("players", currentPlayers + playerId)
            }
        }
    }

    fun startGame(roomId: String) {
        val roomRef = firestore.collection("gameRooms").document(roomId)
        val aliveMap = _uiState.value.playerIds.associateWith { true }

        roomRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.get("type") as? String == "free") {
                val currentPlayers = (snapshot.get("players") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                if (currentPlayers.size >= 2) {
                    roomRef.update(
                        mapOf(
                            "aliveStatus" to aliveMap,
                            "gameStarted" to true
                        )
                    )
                } else {
                    _uiState.value = _uiState.value.copy(errorMessage = "At least two players are required to start the game.", gameStarted = false)
                    return@addOnSuccessListener
                }
            } else if (snapshot.get("type") as? String == "tournament") {
                val teams = snapshot.get("teams") as? Map<*, *> ?: emptyMap<Any?, Any?>()
                val teamMap = mutableMapOf<String, List<String>>()
                var counter = 0

                teams.forEach { (teamName, playerList) ->
                    val players = (playerList as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    if (players.isNotEmpty()) {
                        counter++
                    }
                }
                if (counter == teams.size) {
                    roomRef.update(
                        mapOf(
                            "aliveStatus" to aliveMap,
                            "gameStarted" to true
                        )
                    )
                } else {
                    _uiState.value = _uiState.value.copy(errorMessage = "Each team must have at least one participant to start the game.", gameStarted = false)
                    return@addOnSuccessListener
                }
            }
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
            if (!snapshot.exists()) throw Exception("Кімната не існує")

            val players = (snapshot.get("players") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            val updatedPlayers = players.filterNot { it == playerId }

            if (updatedPlayers.isEmpty()) {
                transaction.delete(roomRef)
            } else {
                transaction.update(roomRef, "players", updatedPlayers)
            }
        }.addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.localizedMessage ?: "Помилка") }
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