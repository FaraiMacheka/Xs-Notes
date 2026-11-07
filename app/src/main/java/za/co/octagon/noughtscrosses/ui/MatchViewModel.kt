package za.co.octagon.noughtscrosses.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import za.co.octagon.noughtscrosses.game.Ai
import za.co.octagon.noughtscrosses.game.Difficulty
import za.co.octagon.noughtscrosses.game.Game
import za.co.octagon.noughtscrosses.game.Outcome
import za.co.octagon.noughtscrosses.game.Player

enum class Mode { TWO_PLAYER, VS_COMPUTER }

data class MatchConfig(
    val mode: Mode = Mode.TWO_PLAYER,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    /** The mark the human plays in [Mode.VS_COMPUTER]. */
    val humanMark: Player = Player.X,
) {
    val computerMark: Player? get() = if (mode == Mode.VS_COMPUTER) humanMark.other else null
}

data class Score(val xWins: Int = 0, val oWins: Int = 0, val draws: Int = 0) {
    operator fun plus(outcome: Outcome): Score = when (outcome) {
        is Outcome.Win -> if (outcome.player == Player.X) copy(xWins = xWins + 1) else copy(oWins = oWins + 1)
        Outcome.Draw -> copy(draws = draws + 1)
    }
}

data class MatchState(
    val config: MatchConfig = MatchConfig(),
    val game: Game = Game.start(Player.X),
    val score: Score = Score(),
    /** Who opened the current round; the opener alternates every round. */
    val roundStarter: Player = Player.X,
    val computerThinking: Boolean = false,
) {
    val isHumanTurn: Boolean
        get() = !game.isOver && !computerThinking && game.toMove != config.computerMark

    val inProgress: Boolean get() = !game.isOver && game.board.emptyCells.size < 9
}

/**
 * Holds one match (a series of rounds with a running score). Scoped to the activity so the
 * session score survives rotation and navigating between screens.
 */
class MatchViewModel(
    private val ai: Ai = Ai(),
    private val computerDelayMs: Long = 400,
) : ViewModel() {

    private val _state = MutableStateFlow(MatchState())
    val state: StateFlow<MatchState> = _state.asStateFlow()

    private var computerJob: Job? = null

    fun startMatch(config: MatchConfig) {
        computerJob?.cancel()
        _state.value = MatchState(config = config)
        maybeComputerMove()
    }

    fun tap(cell: Int) {
        val s = _state.value
        if (!s.isHumanTurn || !s.game.board.isEmpty(cell)) return
        apply(s.game.play(cell))
    }

    fun playAgain() {
        computerJob?.cancel()
        _state.update {
            val starter = it.roundStarter.other
            it.copy(game = Game.start(starter), roundStarter = starter, computerThinking = false)
        }
        maybeComputerMove()
    }

    fun resetScore() {
        _state.update { it.copy(score = Score()) }
    }

    private fun apply(next: Game) {
        _state.update { s ->
            val outcome = next.outcome
            s.copy(game = next, score = if (outcome != null) s.score + outcome else s.score)
        }
        maybeComputerMove()
    }

    private fun maybeComputerMove() {
        val s = _state.value
        val computer = s.config.computerMark ?: return
        if (s.game.isOver || s.game.toMove != computer) return
        _state.update { it.copy(computerThinking = true) }
        computerJob = viewModelScope.launch {
            delay(computerDelayMs) // a short pause so the computer doesn't feel instant
            val current = _state.value
            val move = ai.chooseMove(current.game.board, computer, current.config.difficulty)
            _state.update { it.copy(computerThinking = false) }
            apply(current.game.play(move))
        }
    }
}
