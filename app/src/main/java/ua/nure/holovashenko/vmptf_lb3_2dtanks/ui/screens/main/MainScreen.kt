package ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.main

import android.content.res.Resources.Theme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MainScreen(
    launchLeaderboardScreen: () -> Unit,
    launchCreateGameScreen: (String) -> Unit,
    launchJoinGameScreen: (String) -> Unit,
    launchAuthScreen: () -> Unit,
    launchProfileScreen: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val uid = user?.uid.toString()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Tanks Game",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = launchAuthScreen
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = "Sign in"
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    label = { Text("Main") },
                    onClick = {},
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home"
                        )
                    }
                )
                NavigationBarItem(
                    selected = false,
                    label = { Text("Leaderboard") },
                    onClick = launchLeaderboardScreen,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Leaderboard"
                        )
                    }
                )
                NavigationBarItem(
                    selected = false,
                    label = { Text("Profile") },
                    onClick = launchProfileScreen,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile"
                        )
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to Tanks!",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        launchCreateGameScreen(uid)
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .size(160.dp) // квадратна кнопка
                ) {
                    Text("Create", maxLines = 1, style = MaterialTheme.typography.displaySmall)
                }

                Button(
                    onClick = {
                        launchJoinGameScreen(uid)
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .size(160.dp) // квадратна кнопка
                ) {
                    Text("Join", maxLines = 1, style = MaterialTheme.typography.displaySmall)
                }
            }
        }
    }
}