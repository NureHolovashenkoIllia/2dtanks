package ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.profile

data class GameHistoryItem(
    val gameId: String,
    val datetime: String,
    val durationSeconds: Int,
    val type: String,
    val winner: String
)