package ua.nure.holovashenko.vmptf_lb3_2dtanks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.theme.TanksTheme
import ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.navigation.TanksGame

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TanksTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TanksGame()
                }
            }
        }
    }
}

@Preview(
    showBackground = true
)
@Composable
fun TanksGamePreview() {
    TanksTheme {
        TanksGame()
    }
}