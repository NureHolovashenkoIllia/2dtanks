package ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.leaderboard

data class PlayerStatistic(
    val uid: String,
    val email: String?,
    val wins: Int,
    val kills: Int,
    val matches: Int
)