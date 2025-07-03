package ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.painterResource
import ua.nure.holovashenko.vmptf_lb3_2dtanks.R
import androidx.compose.runtime.getValue

@Composable
fun MainScreen(
    launchLeaderboardScreen: () -> Unit,
    launchCreateGameScreen: (String) -> Unit,
    launchJoinGameScreen: (String) -> Unit,
    launchProfileScreen: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val uid by viewModel.uid.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Mini Tanks", fontWeight = FontWeight.Bold) })
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = true, onClick = { }, icon = {
                    Icon(painterResource(R.drawable.outline_home_24), "Home")
                }, label = { Text("Main") })

                NavigationBarItem(selected = false, onClick = launchLeaderboardScreen, icon = {
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
                    onClick = { launchCreateGameScreen(uid) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(160.dp)
                ) {
                    Text("Create", maxLines = 1, style = MaterialTheme.typography.displaySmall)
                }

                Button(
                    onClick = { launchJoinGameScreen(uid) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(160.dp)
                ) {
                    Text("Join", maxLines = 1, style = MaterialTheme.typography.displaySmall)
                }
            }
        }
    }
}