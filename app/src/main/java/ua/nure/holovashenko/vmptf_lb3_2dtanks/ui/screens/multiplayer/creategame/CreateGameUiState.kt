package ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.multiplayer.creategame

import ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.multiplayer.GameType

data class CreateGameUiState(
    val playersCount: String = "",
    val gameDuration: String = "",
    val teamsCount: String = "",
    val playersPerTeam: String = "",
    val type: GameType = GameType.FREE,
    val isCreating: Boolean = false,
    val errorMessage: String? = null
)