package ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ua.nure.holovashenko.vmptf_lb3_2dtanks.R

@Composable
fun LeaderboardScreen(
    launchMainScreen: () -> Unit,
    launchProfileScreen: () -> Unit,
    viewModel: LeaderboardViewModel = viewModel()
) {
    val leaderboard by viewModel.leaderboard.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    var showChartDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Leaderboard", fontWeight = FontWeight.Bold) })
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = false, onClick = launchMainScreen, icon = {
                    Icon(painterResource(R.drawable.outline_home_24), "Home")
                }, label = { Text("Main") })

                NavigationBarItem(selected = true, onClick = { }, icon = {
                    Icon(painterResource(R.drawable.outline_trophy_24), "Leaderboard")
                }, label = { Text("Leaderboard") })

                NavigationBarItem(selected = false, onClick = launchProfileScreen, icon = {
                    Icon(painterResource(R.drawable.outline_account_circle_24), "Profile")
                }, label = { Text("Profile") })
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sorted by: ${sortOption.label}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.primary
                        ),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { expanded = true }
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        SortOption.entries.forEach { option ->
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

                IconButton(
                    onClick = { showChartDialog = true }
                ) {
                    Icon(
                        painterResource(R.drawable.outline_pie_chart_24),
                        "View Chart",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (leaderboard.isEmpty()) {
                Text("Loading or no players...", style = MaterialTheme.typography.bodyLarge)
            } else {
                LeaderboardTable(leaderboard)
            }
        }

        if (showChartDialog) {
            AlertDialog(
                onDismissRequest = { showChartDialog = false },
                confirmButton = {
                    TextButton(onClick = { showChartDialog = false }) {
                        Text("Close")
                    }
                },
                title = { Text("Kills Distribution") },
                text = { KillsPieChart(players = leaderboard) }
            )
        }
    }
}

@Composable
fun LeaderboardTable(players: List<PlayerStatistic>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, shape = MaterialTheme.shapes.small)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = MaterialTheme.shapes.small
            )
            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableCell("#", weight = 0.5f, fontWeight = FontWeight.Bold)
                TableCell("Nickname", weight = 2f, fontWeight = FontWeight.Bold)
                TableCell("Wins", weight = 1f, fontWeight = FontWeight.Bold)
                TableCell("Matches", weight = 1.3f, fontWeight = FontWeight.Bold)
                TableCell("Destroyed", weight = 1.5f, fontWeight = FontWeight.Bold)
            }
        }

        itemsIndexed(players) { index, player ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (index % 2 == 0) MaterialTheme.colorScheme.surface
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableCell("${index + 1}", weight = 0.5f)
                TableCell(player.nickname ?: "Player", weight = 2f, multiline = true)
                TableCell("${player.wins}", weight = 1f)
                TableCell("${player.matches}", weight = 1.3f)
                TableCell("${player.kills}", weight = 1.5f)
            }
        }
    }
}

@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float,
    fontWeight: FontWeight? = null,
    multiline: Boolean = false
) {
    Box(
        modifier = Modifier
            .weight(weight)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = fontWeight ?: FontWeight.Normal
            ),
            maxLines = if (multiline) Int.MAX_VALUE else 1,
            softWrap = multiline
        )
    }
}