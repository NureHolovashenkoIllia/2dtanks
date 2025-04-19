package ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.leaderboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LeaderboardScreen(
    launchMainScreen: () -> Unit,
    launchProfileScreen: () -> Unit,
    viewModel: LeaderboardViewModel = viewModel()
) {
    val leaderboard by viewModel.leaderboard.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Leaderboard", fontWeight = FontWeight.Bold) },
                actions = {
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Sorting")
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            SortOption.values().forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.label) },
                                    onClick = {
                                        viewModel.setSortOption(option)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    label = { Text("Main") },
                    onClick = launchMainScreen,
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") }
                )
                NavigationBarItem(
                    selected = true,
                    label = { Text("Leaderboard") },
                    onClick = { },
                    icon = { Icon(Icons.Default.Star, contentDescription = "Leaderboard") }
                )
                NavigationBarItem(
                    selected = false,
                    label = { Text("Profile") },
                    onClick = launchProfileScreen,
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(24.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "🏆 Leaderboard",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Sorting: ${sortOption.label}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (leaderboard.isEmpty()) {
                Text("Loading or no players...", style = MaterialTheme.typography.bodyLarge)
            } else {
                LazyColumn {
                    items(leaderboard) { player ->
                        LeaderboardItem(player)
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardItem(player: PlayerStatistic) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = player.email ?: "Player",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text("Won: ${player.wins} | Matches: ${player.matches} | Destroyed: ${player.kills}")
    }
}