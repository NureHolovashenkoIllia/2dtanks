package ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AuthScreen(
    onAuthSuccess: (String) -> Unit // передаємо UID
) {
    val auth = FirebaseAuth.getInstance()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegister by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(if (isRegister) "Registration" else "Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                isLoading = true
                errorMessage = null
                if (isRegister) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                val user = task.result?.user
                                val uid = user?.uid ?: ""
                                if (uid.isNotEmpty()) {
                                    // ✅ Додаємо користувача в Firestore
                                    val firestore = FirebaseFirestore.getInstance()
                                    val userData = hashMapOf("email" to email)
                                    firestore.collection("users").document(uid).set(userData)
                                        .addOnSuccessListener {
                                            onAuthSuccess(uid)
                                        }
                                        .addOnFailureListener { e ->
                                            errorMessage = "Error saving user: ${e.localizedMessage}"
                                        }
                                } else {
                                    errorMessage = "Failed to get user UID"
                                }
                            } else {
                                errorMessage = task.exception?.localizedMessage
                            }
                        }
                } else {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                onAuthSuccess(task.result?.user?.uid ?: "")
                            } else {
                                errorMessage = task.exception?.localizedMessage
                            }
                        }
                }
            },
            enabled = !isLoading
        ) {
            Text(if (isRegister) "Register" else "Login")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = { isRegister = !isRegister }) {
            Text(if (isRegister) "Already have an account? Log in" else "Not registered yet? Create an account")
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}