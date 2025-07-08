package ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.nure.holovashenko.vmptf_lb3_2dtanks.R

@Composable
fun AuthScreen(
    onAuthSuccess: (String) -> Unit,
) {
    val authStrings = rememberAuthStrings()
    val viewModel = remember { AuthViewModel(authStrings) }

    val isRegister by viewModel.isRegister.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isInputValid by viewModel.isInputValid.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val email by viewModel.email.collectAsState()
    val emailError by viewModel.emailError.collectAsState()
    val emailTouched by viewModel.emailTouched.collectAsState()
    val emailEdited by viewModel.emailEdited.collectAsState()

    val password by viewModel.password.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()
    val passwordTouched by viewModel.passwordTouched.collectAsState()
    val passwordEdited by viewModel.passwordEdited.collectAsState()

    val nickname by viewModel.nickname.collectAsState()
    val nicknameError by viewModel.nicknameError.collectAsState()
    val nicknameTouched by viewModel.nicknameTouched.collectAsState()
    val nicknameEdited by viewModel.nicknameEdited.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(if (isRegister) R.string.auth_create_account else R.string.auth_welcome_back),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isRegister) {
                OutlinedTextField(
                    value = nickname,
                    onValueChange = {
                        viewModel.nickname.value = it
                        viewModel.nicknameEdited.value = true
                    },
                    label = { Text(stringResource(R.string.auth_nickname)) },
                    singleLine = true,
                    isError = nicknameTouched && nicknameEdited && nicknameError != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged {
                            if (!it.isFocused) {
                                viewModel.onNicknameBlur()
                            }
                        }
                )
                if (nicknameTouched && nicknameError != null) {
                    Text(
                        text = nicknameError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = {
                    viewModel.email.value = it
                    viewModel.emailEdited.value = true
                },
                label = { Text(stringResource(R.string.auth_email)) },
                singleLine = true,
                isError = emailTouched && emailEdited && emailError != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged {
                        if (!it.isFocused) {
                            viewModel.onEmailBlur()
                        }
                    }
            )
            if (emailTouched && emailError != null) {
                Text(
                    text = emailError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    viewModel.password.value = it
                    viewModel.passwordEdited.value = true
                },
                label = { Text(stringResource(R.string.auth_password)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                isError = passwordTouched && passwordEdited && passwordError != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged {
                        if (!it.isFocused) {
                            viewModel.onPasswordBlur()
                        }
                    }
            )
            if (passwordTouched && passwordError != null) {
                Text(
                    text = passwordError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            ElevatedButton(
                onClick = { viewModel.onAuthClick(onAuthSuccess) },
                enabled = isInputValid && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = stringResource(
                        if (isRegister)
                            R.string.auth_register
                        else
                            R.string.auth_login
                    ),
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = { viewModel.toggleAuthMode() }) {
                Text(
                    text = stringResource(
                        if (isRegister)
                            R.string.auth_toggle_to_login
                        else
                            R.string.auth_toggle_to_register
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun rememberAuthStrings(): AuthStrings {
    return AuthStrings(
        emailEmpty = stringResource(R.string.auth_error_email_empty),
        emailInvalid = stringResource(R.string.auth_error_email_invalid),
        passwordEmpty = stringResource(R.string.auth_error_password_empty),
        passwordShort = stringResource(R.string.auth_error_password_short),
        nicknameEmpty = stringResource(R.string.auth_error_nickname_empty),
        nicknameShort = stringResource(R.string.auth_error_nickname_short),
        nicknameTaken = stringResource(R.string.auth_error_nickname_taken)
    )
}