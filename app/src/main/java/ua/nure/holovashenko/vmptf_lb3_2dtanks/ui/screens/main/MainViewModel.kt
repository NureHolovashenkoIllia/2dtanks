package ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.main

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _uid = MutableStateFlow(auth.currentUser?.uid ?: "")
    val uid: StateFlow<String> get() = _uid
}
