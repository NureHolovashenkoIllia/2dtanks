package ua.nure.holovashenko.vmptf_lb3_2dtanks.ui.screens.leaderboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.tehras.charts.piechart.PieChart
import com.github.tehras.charts.piechart.PieChartData
import com.github.tehras.charts.piechart.animation.simpleChartAnimation

@Composable
fun KillsPieChart(players: List<PlayerStatistic>) {
    val totalKills = players.sumOf { it.kills }
    if (totalKills == 0) {
        Text("No kills recorded.", style = MaterialTheme.typography.bodyLarge)
        return
    }

    val filteredPlayers = players.filter { it.kills > 0 }
    val pieChartData = PieChartData(
        slices = filteredPlayers.mapIndexed { index, player ->
            PieChartData.Slice(
                value = player.kills.toFloat(),
                color = pieColors[index % pieColors.size]
            )
        }
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PieChart(
            pieChartData = pieChartData,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            animation = simpleChartAnimation(),
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            filteredPlayers.forEachIndexed { index, player ->
                val percentage = (player.kills * 100f / totalKills).toInt()
                Text(
                    text = "${index + 1}. ${player.nickname} - ${player.kills} kills ($percentage%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = pieColors[index % pieColors.size],
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

private val pieColors = listOf(
    Color(0xFFE57373),
    Color(0xFF64B5F6),
    Color(0xFF81C784),
    Color(0xFFFFD54F),
    Color(0xFFBA68C8),
    Color(0xFFFF8A65),
    Color(0xFFA1887F),
    Color(0xFF4DB6AC),
    Color(0xFF90A4AE)
)
