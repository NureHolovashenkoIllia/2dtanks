package ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LeaderboardViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _leaderboard = MutableStateFlow<List<PlayerStatistic>>(emptyList())
    val leaderboard: StateFlow<List<PlayerStatistic>> = _leaderboard

    private val _sortOption = MutableStateFlow(SortOption.WINS)
    val sortOption: StateFlow<SortOption> = _sortOption

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
        fetchLeaderboard()
    }

    init {
        fetchLeaderboard()
    }

    private fun fetchLeaderboard() {
        viewModelScope.launch {
            firestore.collection("users")
                .get()
                .addOnSuccessListener { result ->
                    val stats = result.map { doc ->
                        val uid = doc.id
                        val nickname = doc.getString("nickname") ?: "Unknown"
                        val email = doc.getString("email") ?: "Невідомо"
                        val wins = doc.getLong("wins")?.toInt() ?: 0
                        val kills = doc.getLong("kills")?.toInt() ?: 0
                        val matches = doc.getLong("matches")?.toInt() ?: 0

                        PlayerStatistic(uid, nickname, email, wins, kills, matches)
                    }

                    _leaderboard.value = when (_sortOption.value) {
                        SortOption.WINS -> stats.sortedByDescending { it.wins }
                        SortOption.MATCHES -> stats.sortedByDescending { it.matches }
                        SortOption.KILLS -> stats.sortedByDescending { it.kills }
                    }
                }
        }
    }
}