package za.co.stripedape.noughtscrosses.ui

import android.view.SoundEffectConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import za.co.stripedape.noughtscrosses.data.Settings
import za.co.stripedape.noughtscrosses.game.Difficulty
import za.co.stripedape.noughtscrosses.game.Outcome
import za.co.stripedape.noughtscrosses.game.Player
import za.co.stripedape.noughtscrosses.ui.theme.LocalMarkColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    state: MatchState,
    settings: Settings,
    onCellTap: (Int) -> Unit,
    onPlayAgain: () -> Unit,
    onResetScore: () -> Unit,
    onExit: () -> Unit,
) {
    var confirmExit by remember { mutableStateOf(false) }
    val requestExit = { if (state.inProgress) confirmExit = true else onExit() }
    BackHandler(onBack = requestExit)

    val view = LocalView.current
    val haptics = LocalHapticFeedback.current
    val outcome = state.game.outcome
    val movesMade = 9 - state.game.board.emptyCells.size

    // Tap sound whenever a mark lands (human or computer).
    LaunchedEffect(movesMade) {
        if (movesMade > 0 && settings.sound) view.playSoundEffect(SoundEffectConstants.CLICK)
    }
    // Buzz on a win.
    LaunchedEffect(outcome) {
        if (outcome is Outcome.Win && settings.vibration) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
    // Show the result overlay after the winning line has drawn.
    var showResult by remember { mutableStateOf(false) }
    LaunchedEffect(outcome) {
        showResult = false
        if (outcome != null) {
            delay(600)
            showResult = true
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(titleFor(state.config)) },
                navigationIcon = {
                    IconButton(onClick = requestExit) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to home")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            Column(
                Modifier.fillMaxSize().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = statusText(state),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor(state),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
                Spacer(Modifier.height(24.dp))
                BoardView(
                    board = state.game.board,
                    winningLine = (outcome as? Outcome.Win)?.line,
                    enabled = state.isHumanTurn,
                    onCellTap = onCellTap,
                    modifier = Modifier.widthIn(max = 420.dp),
                )
                Spacer(Modifier.height(32.dp))
                ScoreRow(state)
                TextButton(onClick = onResetScore) { Text("Reset score") }
            }

            AnimatedVisibility(
                visible = showResult && outcome != null,
                enter = fadeIn() + scaleIn(initialScale = 0.9f),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center),
            ) {
                if (outcome != null) {
                    ResultCard(resultText(state, outcome), onPlayAgain = onPlayAgain, onHome = onExit)
                }
            }
        }
    }

    if (confirmExit) {
        AlertDialog(
            onDismissRequest = { confirmExit = false },
            title = { Text("Leave this game?") },
            text = { Text("The current round will be lost.") },
            confirmButton = {
                TextButton(onClick = { confirmExit = false; onExit() }) { Text("Leave") }
            },
            dismissButton = {
                TextButton(onClick = { confirmExit = false }) { Text("Keep playing") }
            },
        )
    }
}

@Composable
private fun ScoreRow(state: MatchState) {
    val colors = LocalMarkColors.current
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        ScoreCell(label = nameFor(Player.X, state.config), value = state.score.xWins, color = colors.x)
        ScoreCell(label = "Draws", value = state.score.draws, color = MaterialTheme.colorScheme.onSurfaceVariant)
        ScoreCell(label = nameFor(Player.O, state.config), value = state.score.oWins, color = colors.o)
    }
}

@Composable
private fun ScoreCell(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ResultCard(text: String, onPlayAgain: () -> Unit, onHome: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Column(Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
                )
                Spacer(Modifier.height(24.dp))
                Button(onClick = onPlayAgain, modifier = Modifier.fillMaxWidth()) { Text("Play again") }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onHome, modifier = Modifier.fillMaxWidth()) { Text("Home") }
            }
        }
    }
}

private fun titleFor(config: MatchConfig): String = when (config.mode) {
    Mode.TWO_PLAYER -> "2 Players"
    Mode.VS_COMPUTER -> "Vs Computer · " + when (config.difficulty) {
        Difficulty.EASY -> "Easy"
        Difficulty.MEDIUM -> "Medium"
        Difficulty.HARD -> "Hard"
    }
}

private fun nameFor(player: Player, config: MatchConfig): String = when (config.computerMark) {
    null -> "${player.name} wins"
    player -> "Computer"
    else -> "You"
}

private fun statusText(state: MatchState): String {
    val outcome = state.game.outcome
    if (outcome != null) return resultText(state, outcome)
    if (state.computerThinking) return "Computer is thinking…"
    val toMove = state.game.toMove
    return if (state.config.mode == Mode.VS_COMPUTER) "Your turn (${toMove.name})" else "${toMove.name}'s turn"
}

@Composable
private fun statusColor(state: MatchState): Color {
    val colors = LocalMarkColors.current
    if (state.game.isOver) return MaterialTheme.colorScheme.onBackground
    return if (state.game.toMove == Player.X) colors.x else colors.o
}

private fun resultText(state: MatchState, outcome: Outcome): String = when (outcome) {
    Outcome.Draw -> "It's a draw"
    is Outcome.Win -> when (state.config.computerMark) {
        null -> "${outcome.player.name} wins!"
        outcome.player -> "Computer wins"
        else -> "You win!"
    }
}
