package ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.auth.AuthScreen
import ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.leaderboard.LeaderboardScreen
import ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.main.MainScreen
import ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.multiplayer.creategame.CreateGameScreen
import ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.multiplayer.game.GameScreen
import ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.multiplayer.joingame.JoinGameScreen
import ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.multiplayer.waitingroom.WaitingRoomScreen
import ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.profile.ProfileScreen
import ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.theme.TanksTheme

@Composable
fun TanksGame() {
    val navController = rememberNavController()
    val currentUser = remember { FirebaseAuth.getInstance().currentUser }
    val startDestination = if (currentUser != null) MainRoute else AuthRoute

    TanksTheme {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable(MainRoute) {
                MainScreen(
                    launchLeaderboardScreen = { navController.navigate(LeaderboardRoute) },
                    launchCreateGameScreen = { uid ->
                        navController.navigate("$CreateGameRoute/$uid")
                    },
                    launchJoinGameScreen = { uid ->
                        navController.navigate("$JoinGameRoute/$uid")
                    },
                    launchProfileScreen = { navController.navigate(ProfileRoute) }
                )
            }
            composable(LeaderboardRoute) {
                LeaderboardScreen(launchMainScreen = { navController.navigate(MainRoute) },
                    launchProfileScreen = { navController.navigate(ProfileRoute) })
            }
            composable("$CreateGameRoute/{userId}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")
                userId?.let {
                    CreateGameScreen(
                        onRoomCreated = { roomId, userId, isCreator ->
                            navController.navigate("$WaitingRoomRoute/$roomId/$userId/$isCreator")
                        },
                        currentPlayerId = userId,
                        onNavigateBack = {
                            navController.navigateUp()
                        }
                    )
                }
            }
            composable("$WaitingRoomRoute/{roomId}/{userId}/{isCreator}?fromGame={fromGame}") { backStackEntry ->
                val roomId = backStackEntry.arguments?.getString("roomId")
                val playerId = backStackEntry.arguments?.getString("userId")
                val isCreator = backStackEntry.arguments?.getString("isCreator")?.toBooleanStrictOrNull()
                val fromGame = backStackEntry.arguments?.getString("fromGame")?.toBooleanStrictOrNull() ?: false

                if (roomId != null && playerId != null && isCreator != null) {
                    WaitingRoomScreen(
                        roomId = roomId,
                        currentPlayerId = playerId,
                        isCreator = isCreator,
                        fromGame = fromGame,
                        onNavigateToMain = {
                            navController.navigate(MainRoute) {
                                popUpTo(MainRoute) { inclusive = true }
                            }
                        },
                        onStartGame = {
                            navController.navigate("$GameRoute/$roomId/$playerId/$isCreator")
                        }
                    )
                }
            }
            composable("$JoinGameRoute/{userId}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")
                userId?.let {
                    JoinGameScreen(
                        currentPlayerId = userId,
                        onJoinSuccess = { roomId, userId, isCreator ->
                            navController.navigate("$WaitingRoomRoute/$roomId/$userId/$isCreator")
                        },
                        onNavigateBack = {
                            navController.navigateUp()
                        }
                    )
                }
            }
            composable(AuthRoute) {
                AuthScreen(
                    onAuthSuccess = { uid ->
                        navController.navigate(MainRoute) {
                            popUpTo(AuthRoute) { inclusive = true }
                        }
                    }
                )
            }
            composable(ProfileRoute) {
                ProfileScreen(onLogout = {
                    navController.navigate(AuthRoute) {
                        popUpTo(MainRoute) { inclusive = true }
                    }
                },
                    launchLeaderboardScreen = { navController.navigate(LeaderboardRoute) },
                    launchMainScreen = { navController.navigate(MainRoute) })
            }
            composable("$GameRoute/{roomId}/{playerId}/{isCreator}") { backStackEntry ->
                val roomId = backStackEntry.arguments?.getString("roomId")
                val playerId = backStackEntry.arguments?.getString("playerId")
                val isCreator = backStackEntry.arguments?.getBoolean("isCreator")
                roomId?.let {
                    if (playerId != null) {
                        GameScreen(roomId = it,
                            currentPlayerId = playerId,
                            onGameEnd = {
                                navController.navigate(MainRoute) {
                                    popUpTo(MainRoute) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}