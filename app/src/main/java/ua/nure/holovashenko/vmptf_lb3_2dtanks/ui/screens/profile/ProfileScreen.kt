package ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
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
    val nickname by viewModel.nickname
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
                    Icon(painterResource(R.drawable.ic_home), "Home")
                }, label = { Text("Main") })

                NavigationBarItem(selected = false, onClick = launchLeaderboardScreen, icon = {
                    Icon(painterResource(R.drawable.ic_leaderboard), "Leaderboard")
                }, label = { Text("Leaderboard") })

                NavigationBarItem(selected = true, onClick = {}, icon = {
                    Icon(painterResource(R.drawable.ic_account_circle), "Profile")
                }, label = { Text("Profile") })
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            if (user == null) {
                Text("User is not authorized", style = MaterialTheme.typography.bodyLarge)
                return@Column
            }

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "User Information",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Nickname: $nickname", style = MaterialTheme.typography.bodyLarge)
                    Text("Email: ${user.email ?: "Unknown"}", style = MaterialTheme.typography.bodyLarge)
                    Text("Games won: ${gamesWon ?: "Loading..."}", style = MaterialTheme.typography.bodyLarge)
                    Text("Games played: ${gamesPlayed ?: "Loading..."}", style = MaterialTheme.typography.bodyLarge)
                    Text("Tanks destroyed: ${tanksDestroyed ?: "Loading..."}", style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(Modifier.height(32.dp))

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Match History",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))

                    if (matchHistory.isEmpty()) {
                        Text("No match history available.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        MatchHistoryTable(matchHistory)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = { viewModel.logout(onLogout) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Log out", color = MaterialTheme.colorScheme.onError)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun MatchHistoryTable(matches: List<GameHistoryItem>) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .shadow(4.dp, shape = MaterialTheme.shapes.small)
        .background(
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = MaterialTheme.shapes.small
        )) {

        Row(modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(vertical = 8.dp)) {
            TableHeader("#", 0.7f)
            TableHeader("Date", 1.65f)
            TableHeader("Duration", 1.3f)
            TableHeader("Type", 1f)
            TableHeader("Winner", 1.2f)
        }

        matches.forEachIndexed { index, match ->
            val bgColor = if (index % 2 == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgColor)
                    .padding(vertical = 8.dp)
            ) {
                TableCell(match.gameId, 0.7f)
                TableCell(match.datetime, 1.65f)
                TableCell("${match.durationSeconds}s", 1.3f)
                TableCell(match.type, 1f)
                TableCell(match.winner, 1.2f)
            }
        }
    }
}

@Composable
fun RowScope.TableHeader(text: String, weight: Float) {
    Text(
        text = text,
        modifier = Modifier
            .weight(weight)
            .padding(horizontal = 8.dp),
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.labelLarge
    )
}

@Composable
fun RowScope.TableCell(text: String, weight: Float) {
    Text(
        text = text,
        modifier = Modifier
            .weight(weight)
            .padding(horizontal = 8.dp),
        style = MaterialTheme.typography.bodyMedium,
        maxLines = 1
    )
}
