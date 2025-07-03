package ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.profile

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val user = auth.currentUser

    private val _gamesWon = mutableStateOf<Int?>(null)
    val gamesWon: State<Int?> = _gamesWon

    private val _gamesPlayed = mutableStateOf<Int?>(null)
    val gamesPlayed: State<Int?> = _gamesPlayed

    private val _tanksDestroyed = mutableStateOf<Int?>(null)
    val tanksDestroyed: State<Int?> = _tanksDestroyed

    private val _matchHistory = mutableStateOf<List<GameHistoryItem>>(emptyList())
    val matchHistory: State<List<GameHistoryItem>> = _matchHistory

    init {
        loadStats()
    }

    private fun loadStats() {
        val uid = user?.uid ?: return

        viewModelScope.launch {
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    _gamesWon.value = doc.getLong("wins")?.toInt()
                    _gamesPlayed.value = doc.getLong("matches")?.toInt()
                    _tanksDestroyed.value = doc.getLong("kills")?.toInt()
                }

            val freeMatches = firestore.collection("gameHistory")
                .whereArrayContains("players", uid)
                .get()
                .await()
                .documents

            val tournamentMatches = firestore.collection("gameHistory")
                .get()
                .await()
                .documents.filter { doc ->
                    val teams = doc.get("teams") as? Map<*, *> ?: return@filter false
                    teams.values.any { team ->
                        (team as? List<*>)?.contains(uid) == true
                    }
                }

            val allMatches = (freeMatches + tournamentMatches).distinctBy { it.id }

            val games = allMatches.mapNotNull { doc ->
                try {
                    val gameId = doc.getString("gameId") ?: return@mapNotNull null
                    val datetime = doc.getDate("datetime") ?: return@mapNotNull null
                    val formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(datetime)
                    val duration = (doc.getLong("durationSeconds") ?: 0).toInt()
                    val type = doc.getString("type") ?: "free"
                    val winnerUid = doc.getString("winner") ?: "unknown"

                    val winnerDisplay = if (type == "tournament") {
                        winnerUid
                    } else {
                        try {
                            val userDoc = firestore.collection("users").document(winnerUid).get().await()
                            userDoc.getString("email") ?: "Unknown"
                        } catch (_: Exception) {
                            "Unknown"
                        }
                    }

                    GameHistoryItem(gameId, formattedDate, duration, type, winnerDisplay)
                } catch (_: Exception) {
                    null
                }
            }

            _matchHistory.value = games.sortedByDescending { it.datetime }
        }
    }

    fun logout(onLogout: () -> Unit) {
        auth.signOut()
        onLogout()
    }
}
