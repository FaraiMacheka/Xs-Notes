package za.co.octagon.noughtscrosses

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import za.co.octagon.noughtscrosses.ui.GameScreen
import za.co.octagon.noughtscrosses.ui.HomeScreen
import za.co.octagon.noughtscrosses.ui.MatchViewModel
import za.co.octagon.noughtscrosses.ui.SettingsScreen
import za.co.octagon.noughtscrosses.ui.SettingsViewModel
import za.co.octagon.noughtscrosses.ui.theme.NoughtsCrossesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            NoughtsCrossesTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    App()
                }
            }
        }
    }
}

private object Routes {
    const val HOME = "home"
    const val GAME = "game"
    const val SETTINGS = "settings"
}

@Composable
private fun App() {
    val nav = rememberNavController()
    // Activity-scoped, so the match and its score survive rotation and screen changes.
    val match: MatchViewModel = viewModel()
    val settingsVm: SettingsViewModel = viewModel()
    val matchState by match.state.collectAsStateWithLifecycle()
    val settings by settingsVm.settings.collectAsStateWithLifecycle()

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
