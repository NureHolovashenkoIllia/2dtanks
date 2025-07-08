package ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.leaderboard

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LeaderboardViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _leaderboard = MutableStateFlow<List<PlayerStatistic>>(emptyList())
    val leaderboard: StateFlow<List<PlayerStatistic>> = _leaderboard

    private val _sortOption = MutableStateFlow(SortOption.WINS)
    val sortOption: StateFlow<SortOption> = _sortOption

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    fun fetchLeaderboard(notAvailable: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _leaderboard.value = emptyList()

            try {
                val result = firestore.collection("users").get().await()

                val stats = result.map { doc ->
                    val uid = doc.id
                    val nickname = doc.getString("nickname") ?: notAvailable
                    val email = doc.getString("email") ?: notAvailable
                    val wins = doc.getLong("wins")?.toInt() ?: 0
                    val kills = doc.getLong("kills")?.toInt() ?: 0
                    val matches = doc.getLong("matches")?.toInt() ?: 0

                    PlayerStatistic(uid, nickname, email, wins, kills, matches)
                }

                val filteredStats = stats.filter { it.wins > 0 && it.kills > 0 && it.matches > 0 }

                _leaderboard.value = when (_sortOption.value) {
                    SortOption.WINS -> filteredStats.sortedByDescending { it.wins }
                    SortOption.MATCHES -> filteredStats.sortedByDescending { it.matches }
                    SortOption.KILLS -> filteredStats.sortedByDescending { it.kills }
                }
            } catch (_: Exception) {
                _leaderboard.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}