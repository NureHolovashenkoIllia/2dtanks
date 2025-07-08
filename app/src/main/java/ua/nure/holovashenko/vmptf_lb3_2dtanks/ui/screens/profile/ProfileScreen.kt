package ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ua.nure.holovashenko.vmptf_lb3_2dtanks.R
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    launchLeaderboardScreen: () -> Unit,
    launchMainScreen: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val notAvailable = stringResource(R.string.not_available)
    val dateFormat = stringResource(R.string.date_format)
    val dateFormatter: (Date) -> String = remember {
        { date -> SimpleDateFormat(dateFormat, context.resources.configuration.locales[0]).format(date) }
    }

    val user = viewModel.user
    val isLoading by viewModel.isLoading
    val nickname by viewModel.nickname
    val gamesWon by viewModel.gamesWon
    val gamesPlayed by viewModel.gamesPlayed
    val tanksDestroyed by viewModel.tanksDestroyed
    val matchHistory by viewModel.matchHistory

    LaunchedEffect(Unit) {
        viewModel.loadStats(notAvailable, dateFormatter)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(stringResource(R.string.profile), fontWeight = FontWeight.Bold) })
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = false, onClick = launchMainScreen, icon = {
                    Icon(painterResource(R.drawable.ic_home), stringResource(R.string.main))
                }, label = { Text(stringResource(R.string.main)) })

                NavigationBarItem(selected = false, onClick = launchLeaderboardScreen, icon = {
                    Icon(painterResource(R.drawable.ic_leaderboard), stringResource(R.string.leaderboard))
                }, label = { Text(stringResource(R.string.leaderboard)) })

                NavigationBarItem(selected = true, onClick = {}, icon = {
                    Icon(painterResource(R.drawable.ic_account_circle), stringResource(R.string.profile))
                }, label = { Text(stringResource(R.string.profile)) })
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
                Text(stringResource(R.string.user_not_authorized), style = MaterialTheme.typography.bodyLarge)
                return@Column
            }

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.user_info),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.nickname, nickname ?: stringResource(R.string.not_available)),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = stringResource(R.string.email, user.email ?: stringResource(R.string.not_available)),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = stringResource(R.string.games_won, gamesWon?.toString() ?: stringResource(R.string.loading)),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = stringResource(R.string.games_played, gamesPlayed?.toString() ?: stringResource(R.string.loading)),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = stringResource(R.string.tanks_destroyed, tanksDestroyed?.toString() ?: stringResource(R.string.loading)),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.match_history),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))

                    if (isLoading) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = stringResource(R.string.loading_match_history),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else if (matchHistory.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_match_history),
                            style = MaterialTheme.typography.bodyMedium
                        )
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
                Text(
                    text = stringResource(R.string.logout),
                    color = MaterialTheme.colorScheme.onError
                )
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
            TableHeader(stringResource(R.string.header_id), 0.7f)
            TableHeader(stringResource(R.string.header_date), 1.65f)
            TableHeader(stringResource(R.string.header_duration), 1.3f)
            TableHeader(stringResource(R.string.header_type), 1f)
            TableHeader(stringResource(R.string.header_winner), 1.2f)
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
                TableCell(stringResource(R.string.second_format, match.durationSeconds), 1.3f)
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