package ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.multiplayer.joingame

import ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.multiplayer.GameType

data class JoinGameUiState(
    val roomCode: String = "",
    val teamName: String = "",
    val gameType: GameType = GameType.FREE,
    val isJoining: Boolean = false,
    val errorMessage: String? = null
)