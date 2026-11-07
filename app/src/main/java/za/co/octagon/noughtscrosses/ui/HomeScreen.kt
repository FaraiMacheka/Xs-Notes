package za.co.octagon.noughtscrosses.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import za.co.octagon.noughtscrosses.game.Board
import za.co.octagon.noughtscrosses.game.Difficulty
import za.co.octagon.noughtscrosses.game.Player

@Composable
fun HomeScreen(
    onStart: (MatchConfig) -> Unit,
    onSettings: () -> Unit,
) {
    var showComputerPicker by rememberSaveable { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().safeDrawingPadding()) {
        IconButton(onClick = onSettings, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
            Icon(Icons.Filled.Settings, contentDescription = "Settings")
        }

        Column(
            Modifier.align(Alignment.Center).padding(32.dp).widthIn(max = 360.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Decorative board: X . O / . X . / O . X
            val demo = Board()
                .place(0, Player.X).place(2, Player.O).place(4, Player.X).place(6, Player.O).place(8, Player.X)
            BoardView(board = demo, winningLine = null, enabled = false, onCellTap = {}, modifier = Modifier.size(160.dp))
            Spacer(Modifier.height(24.dp))
            Text(
                "Noughts & Crosses",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(40.dp))
            Button(
                onClick = { onStart(MatchConfig(mode = Mode.TWO_PLAYER)) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) { Text("2 Players", style = MaterialTheme.typography.titleMedium) }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { showComputerPicker = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) { Text("Vs Computer", style = MaterialTheme.typography.titleMedium) }
        }
    }

    if (showComputerPicker) {
        ComputerPicker(
            onDismiss = { showComputerPicker = false },
            onStart = { showComputerPicker = false; onStart(it) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComputerPicker(onDismiss: () -> Unit, onStart: (MatchConfig) -> Unit) {
    var difficulty by rememberSaveable { mutableStateOf(Difficulty.MEDIUM) }
    var mark by rememberSaveable { mutableStateOf(Player.X) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Vs Computer") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Difficulty", style = MaterialTheme.typography.labelLarge)
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    val options = Difficulty.entries
                    options.forEachIndexed { i, d ->
                        SegmentedButton(
                            selected = difficulty == d,
                            onClick = { difficulty = d },
                            shape = SegmentedButtonDefaults.itemShape(i, options.size),
                        ) { Text(d.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("Play as", style = MaterialTheme.typography.labelLarge)
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    Player.entries.forEachIndexed { i, p ->
                        SegmentedButton(
                            selected = mark == p,
                            onClick = { mark = p },
                            shape = SegmentedButtonDefaults.itemShape(i, 2),
                        ) { Text(p.name) }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onStart(MatchConfig(mode = Mode.VS_COMPUTER, difficulty = difficulty, humanMark = mark))
            }) { Text("Start") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
