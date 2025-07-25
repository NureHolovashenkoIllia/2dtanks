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
import androidx.compose.ui.res.stringResource

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
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.app_name), fontWeight = FontWeight.Bold) }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = true, onClick = { }, icon = {
                    Icon(painterResource(R.drawable.ic_home), stringResource(R.string.main))
                }, label = { Text(stringResource(R.string.main)) })

                NavigationBarItem(selected = false, onClick = launchLeaderboardScreen, icon = {
                    Icon(painterResource(R.drawable.ic_leaderboard), stringResource(R.string.leaderboard))
                }, label = { Text(stringResource(R.string.leaderboard)) })

                NavigationBarItem(selected = false, onClick = launchProfileScreen, icon = {
                    Icon(painterResource(R.drawable.ic_account_circle), stringResource(R.string.profile))
                }, label = { Text(stringResource(R.string.profile)) })
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
                text = stringResource(R.string.welcome),
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
                    Text(
                        text = stringResource(R.string.create),
                        maxLines = 1,
                        style = MaterialTheme.typography.displaySmall
                    )
                }

                Button(
                    onClick = { launchJoinGameScreen(uid) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(160.dp)
                ) {
                    Text(
                        text = stringResource(R.string.join),
                        maxLines = 1,
                        style = MaterialTheme.typography.displaySmall
                    )
                }
            }
        }
    }
}