package ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.multiplayer.game

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class GameViewModelFactory(
    private val application: Application,
    private val roomId: String,
    private val currentPlayerId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(application, roomId, currentPlayerId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
