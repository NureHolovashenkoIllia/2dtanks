package ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    var email = MutableStateFlow("")
    var password = MutableStateFlow("")
    var nickname = MutableStateFlow("")
    var isRegister = MutableStateFlow(false)

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _isInputValid = MutableStateFlow(false)
    val isInputValid: StateFlow<Boolean> get() = _isInputValid

    var emailTouched = MutableStateFlow(false)
    var passwordTouched = MutableStateFlow(false)
    var nicknameTouched = MutableStateFlow(false)

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> get() = _emailError

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> get() = _passwordError

    private val _nicknameError = MutableStateFlow<String?>(null)
    val nicknameError: StateFlow<String?> get() = _nicknameError

    var emailEdited = MutableStateFlow(false)
    var passwordEdited = MutableStateFlow(false)
    var nicknameEdited = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            combine(
                combine(email, password, nickname) { e, p, n -> Triple(e, p, n) },
                combine(emailTouched, passwordTouched, nicknameTouched) { et, pt, nt -> Triple(et, pt, nt) }
            ) { (email, password, nickname), (emailTouched, passwordTouched, nicknameTouched) ->
                val emailValid = validateEmail(email) == null
                val passwordValid = validatePassword(password) == null
                val nicknameValid = if (isRegister.value) nickname.isNotBlank() else true
                val touched = emailTouched && passwordTouched && (!isRegister.value || nicknameTouched)
                touched && emailValid && passwordValid && nicknameValid
            }.collect { isValid ->
                _isInputValid.value = isValid
            }
        }
    }

    fun onAuthClick(onAuthSuccess: (String) -> Unit) {
        _isLoading.value = true
        _errorMessage.value = null

        if (isRegister.value) {
            viewModelScope.launch {
                val nicknameValidation = validateNicknameUniqueness(nickname.value)
                if (nicknameValidation != null) {
                    _nicknameError.value = nicknameValidation
                    _isLoading.value = false
                    return@launch
                }

                auth.createUserWithEmailAndPassword(email.value, password.value)
                    .addOnCompleteListener { task ->
                        _isLoading.value = false
                        if (task.isSuccessful) {
                            val uid = task.result?.user?.uid ?: ""
                            if (uid.isNotEmpty()) {
                                val userData = hashMapOf(
                                    "email" to email.value,
                                    "nickname" to nickname.value
                                )
                                firestore.collection("users").document(uid).set(userData)
                                    .addOnSuccessListener { onAuthSuccess(uid) }
                                    .addOnFailureListener {
                                        _errorMessage.value =
                                            "Error saving user: ${it.localizedMessage}"
                                    }
                            } else {
                                _errorMessage.value = "Failed to get user UID"
                            }
                        } else {
                            _errorMessage.value = task.exception?.localizedMessage
                        }
                    }
            }
        } else {
            auth.signInWithEmailAndPassword(email.value, password.value)
                .addOnCompleteListener { task ->
                    _isLoading.value = false
                    if (task.isSuccessful) {
                        onAuthSuccess(task.result?.user?.uid ?: "")
                    } else {
                        _errorMessage.value = task.exception?.localizedMessage
                    }
                }
        }
    }

    fun toggleAuthMode() {
        isRegister.value = !isRegister.value
    }

    fun onEmailBlur() {
        emailTouched.value = true
        if (emailEdited.value) {
            _emailError.value = validateEmail(email.value)
        }
    }

    fun onPasswordBlur() {
        passwordTouched.value = true
        if (passwordEdited.value) {
            _passwordError.value = validatePassword(password.value)
        }
    }

    fun onNicknameBlur() {
        nicknameTouched.value = true
        if (nicknameEdited.value) {
            viewModelScope.launch {
                _nicknameError.value = validateNicknameUniqueness(nickname.value)
            }
        }
    }

    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email cannot be empty"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email format"
            else -> null
        }
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Password cannot be empty"
            password.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
    }

    private suspend fun validateNicknameUniqueness(nickname: String): String? {
        if (nickname.isBlank()) return "Nickname cannot be empty"
        if (nickname.length < 3) return "Nickname must be at least 3 characters"
        val snapshot = firestore.collection("users")
            .whereEqualTo("nickname", nickname)
            .get().await()
        return if (!snapshot.isEmpty) "Nickname already taken" else null
    }
}
