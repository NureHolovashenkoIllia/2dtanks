package ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ua.nure.holovashenko.vmptf_lb3_2dtanks.R

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    launchLeaderboardScreen: () -> Unit,
    launchMainScreen: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val user = viewModel.user
    val gamesWon by viewModel.gamesWon
    val gamesPlayed by viewModel.gamesPlayed
    val tanksDestroyed by viewModel.tanksDestroyed
    val matchHistory by viewModel.matchHistory

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Profile", fontWeight = FontWeight.Bold) })
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = false, onClick = launchMainScreen, icon = {
                    Icon(painterResource(R.drawable.outline_home_24), "Home")
                }, label = { Text("Main") })

                NavigationBarItem(selected = false, onClick = launchLeaderboardScreen, icon = {
                    Icon(painterResource(R.drawable.outline_trophy_24), "Leaderboard")
                }, label = { Text("Leaderboard") })

                NavigationBarItem(selected = true, onClick = {}, icon = {
                    Icon(painterResource(R.drawable.outline_account_circle_24), "Profile")
                }, label = { Text("Profile") })
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            user?.let {
                Text("Email: ${it.email ?: "Unknown"}", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(24.dp))

                Text("Statistics", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("Games won: ${gamesWon ?: "Loading..."}")
                Text("Games played: ${gamesPlayed ?: "Loading..."}")
                Text("Tanks destroyed: ${tanksDestroyed ?: "Loading..."}")

                Spacer(Modifier.height(32.dp))
                Text("History of matches", style = MaterialTheme.typography.titleMedium)

                if (matchHistory.isEmpty()) {
                    Text("History is not found or loading...")
                } else {
                    matchHistory.forEach { match ->
                        Spacer(Modifier.height(12.dp))
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Game: ${match.gameId}")
                                Text("Time: ${match.datetime}")
                                Text("Duration: ${match.durationSeconds} seconds")
                                Text("Type: ${match.type}")
                                Text("Winner: ${match.winner}")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = { viewModel.logout(onLogout) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Log out")
                }
            } ?: Text("User is not authorized")
        }
    }
}