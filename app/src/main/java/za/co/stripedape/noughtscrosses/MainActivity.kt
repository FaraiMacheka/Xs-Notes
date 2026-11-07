package za.co.stripedape.noughtscrosses

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import za.co.stripedape.noughtscrosses.game.Difficulty
import za.co.stripedape.noughtscrosses.ui.GameScreen
import za.co.stripedape.noughtscrosses.ui.MatchConfig
import za.co.stripedape.noughtscrosses.ui.Mode
import za.co.stripedape.noughtscrosses.ui.Score
import za.co.stripedape.noughtscrosses.ui.HomeScreen
import za.co.stripedape.noughtscrosses.ui.MatchViewModel
import za.co.stripedape.noughtscrosses.ui.SettingsScreen
import za.co.stripedape.noughtscrosses.ui.SettingsViewModel
import za.co.stripedape.noughtscrosses.ui.theme.NoughtsCrossesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val demo = if (BuildConfig.DEBUG) DemoRequest.from(intent) else null
        setContent {
            NoughtsCrossesTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    App(demo)
                }
            }
        }
    }
}

/**
 * Debug-only shortcut for store screenshots: open the game screen on a preset position.
 *   adb shell am start -n za.co.stripedape.noughtscrosses/.MainActivity \
 *     --es demo_mode hard --es demo_moves 4,0,8 --es demo_score 2,1,3
 * demo_mode: two | easy | medium | hard (human plays X). Moves alternate X, O, X...
 * demo_score: X wins, O wins, draws.
 */
private data class DemoRequest(val config: MatchConfig, val moves: List<Int>, val score: Score) {
    companion object {
        fun from(intent: Intent?): DemoRequest? {
            val mode = intent?.getStringExtra("demo_mode") ?: return null
            val config = when (mode) {
                "two" -> MatchConfig(mode = Mode.TWO_PLAYER)
                else -> MatchConfig(mode = Mode.VS_COMPUTER, difficulty = Difficulty.valueOf(mode.uppercase()))
            }
            val moves = intent.getStringExtra("demo_moves").orEmpty()
                .split(',').filter { it.isNotBlank() }.map { it.trim().toInt() }
            val s = intent.getStringExtra("demo_score").orEmpty()
                .split(',').mapNotNull { it.trim().toIntOrNull() }
            val score = if (s.size == 3) Score(xWins = s[0], oWins = s[1], draws = s[2]) else Score()
            return DemoRequest(config, moves, score)
        }
    }
}

private object Routes {
    const val HOME = "home"
    const val GAME = "game"
    const val SETTINGS = "settings"
}

@Composable
private fun App(demo: DemoRequest?) {
    val nav = rememberNavController()
    // Activity-scoped, so the match and its score survive rotation and screen changes.
    val match: MatchViewModel = viewModel()
    val settingsVm: SettingsViewModel = viewModel()
    val matchState by match.state.collectAsStateWithLifecycle()
    val settings by settingsVm.settings.collectAsStateWithLifecycle()

    if (demo != null) {
        LaunchedEffect(Unit) {
            match.loadDemo(demo.config, demo.moves, demo.score)
            nav.navigate(Routes.GAME)
        }
    }

    NavHost(navController = nav, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onStart = { config ->
                    match.startMatch(config)
                    nav.navigate(Routes.GAME)
                },
                onSettings = { nav.navigate(Routes.SETTINGS) },
            )
        }
        composable(Routes.GAME) {
            GameScreen(
                state = matchState,
                settings = settings,
                onCellTap = match::tap,
                onPlayAgain = match::playAgain,
                onResetScore = match::resetScore,
                onExit = { nav.popBackStack(Routes.HOME, inclusive = false) },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                settings = settings,
                onSoundChange = settingsVm::setSound,
                onVibrationChange = settingsVm::setVibration,
                onBack = { nav.popBackStack() },
            )
        }
    }
}
