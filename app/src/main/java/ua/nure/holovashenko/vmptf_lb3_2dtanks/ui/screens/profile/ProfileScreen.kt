package ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    launchLeaderboardScreen: () -> Unit,
    launchMainScreen: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val firestore = FirebaseFirestore.getInstance()

    var gamesWon by remember { mutableStateOf<Int?>(null) }
    var gamesPlayed by remember { mutableStateOf<Int?>(null) }
    var tanksDestroyed by remember { mutableStateOf<Int?>(null) }
    var matchHistory by remember { mutableStateOf<List<GameHistoryItem>>(emptyList()) }

    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    gamesWon = document.getLong("wins")?.toInt() ?: 0
                    gamesPlayed = document.getLong("matches")?.toInt() ?: 0
                    tanksDestroyed = document.getLong("kills")?.toInt() ?: 0
                }

            val freeMatches = firestore.collection("gameHistory")
                .whereArrayContains("players", uid)
                .get()
                .await()
                .documents

            val tournamentMatches = firestore.collection("gameHistory")
                .get()
                .await()
                .documents.filter { doc ->
                    val teams = doc.get("teams") as? Map<*, *> ?: return@filter false
                    teams.values.any { team ->
                        (team as? List<*>)?.contains(uid) == true
                    }
                }

            val allMatches = (freeMatches + tournamentMatches).distinctBy { it.id }

            val games = allMatches.mapNotNull { doc ->
                try {
                    val gameId = doc.getString("gameId") ?: return@mapNotNull null
                    val datetime = doc.getDate("datetime") ?: return@mapNotNull null
                    val formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(datetime)
                    val duration = (doc.getLong("durationSeconds") ?: 0).toInt()
                    val type = doc.getString("type") ?: "free"
                    val winnerUid = doc.getString("winner") ?: "unknown"

                    val winnerDisplay = if (type == "tournament") {
                        winnerUid // це назва команди
                    } else {
                        try {
                            val userDoc = firestore.collection("users").document(winnerUid).get().await()
                            userDoc.getString("email") ?: "Unknown"
                        } catch (e: Exception) {
                            "Unknown"
                        }
                    }

                    GameHistoryItem(gameId, formattedDate, duration, type, winnerDisplay)
                } catch (e: Exception) {
                    null
                }
            }

            matchHistory = games.sortedByDescending { it.datetime }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    label = { Text("Main") },
                    onClick = launchMainScreen,
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
                    selected = true,
                    label = { Text("Profile") },
                    onClick = {  },
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
                .padding(24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            user?.let {
                Text("Email: ${it.email ?: "Unknown"}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(24.dp))

                Text("Statistics", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                Text("Games won: ${gamesWon ?: "Loading..."}")
                Text("Games played: ${gamesPlayed ?: "Loading..."}")
                Text("Tanks destroyed: ${tanksDestroyed ?: "Loading..."}")

                Spacer(modifier = Modifier.height(32.dp))
                Text("History of matches", style = MaterialTheme.typography.titleMedium)

                if (matchHistory.isEmpty()) {
                    Text("History is not found or loading...")
                } else {
                    matchHistory.forEach { match ->
                        Spacer(modifier = Modifier.height(12.dp))
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
            } ?: Text("User is not authorized")


            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    auth.signOut()
                    onLogout()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Log out")
            }
        }
    }
}