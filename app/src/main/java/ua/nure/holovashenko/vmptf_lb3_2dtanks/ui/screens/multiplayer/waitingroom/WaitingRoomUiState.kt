package ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.multiplayer.waitingroom

data class WaitingRoomUiState(
    val playerIds: List<String> = emptyList(),
    val playerEmails: List<String> = emptyList(),
    val gameStarted: Boolean = false,
    val roomType: String = "free", // "free" або "tournament"
    val teamPlayers: Map<String, List<String>> = emptyMap(), // команда -> список id
    val teamEmails: Map<String, List<String>> = emptyMap(),  // команда -> список email
    val errorMessage: String? = null
)