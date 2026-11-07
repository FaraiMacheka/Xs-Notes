package za.co.octagon.noughtscrosses.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import za.co.octagon.noughtscrosses.game.Ai
import za.co.octagon.noughtscrosses.game.Difficulty
import za.co.octagon.noughtscrosses.game.Outcome
import za.co.octagon.noughtscrosses.game.Player
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class MatchViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private fun vm() = MatchViewModel(Ai(Random(1)), computerDelayMs = 400)

    @Test
    fun `two player win updates score and play again alternates starter`() = runTest(dispatcher) {
        val vm = vm()
        vm.startMatch(MatchConfig(mode = Mode.TWO_PLAYER))
        listOf(0, 3, 1, 4, 2).forEach(vm::tap) // X wins the top row
        val s = vm.state.value
        assertEquals(Player.X, (s.game.outcome as Outcome.Win).player)
        assertEquals(1, s.score.xWins)

        vm.playAgain()
        assertEquals(Player.O, vm.state.value.game.toMove)
        assertNull(vm.state.value.game.outcome)
        assertEquals(1, vm.state.value.score.xWins) // score kept across rounds
    }

    @Test
    fun `taps are ignored after the game ends and on taken cells`() = runTest(dispatcher) {
        val vm = vm()
        vm.startMatch(MatchConfig(mode = Mode.TWO_PLAYER))
        vm.tap(4)
        vm.tap(4) // taken: ignored, still O to move
        assertEquals(Player.O, vm.state.value.game.toMove)
        listOf(0, 3, 1, 5).forEach(vm::tap) // O: 0,1  X: 4,3,5 -> X wins middle row
        assertTrue(vm.state.value.game.isOver)
        vm.tap(8)
        assertEquals(1, vm.state.value.score.xWins)
    }

    @Test
    fun `computer replies after its delay and blocks human taps meanwhile`() = runTest(dispatcher) {
        val vm = vm()
        vm.startMatch(MatchConfig(mode = Mode.VS_COMPUTER, difficulty = Difficulty.HARD, humanMark = Player.X))
        vm.tap(0)
        assertTrue(vm.state.value.computerThinking)
        vm.tap(1) // ignored while the computer is thinking
        assertEquals(1, 9 - vm.state.value.game.board.emptyCells.size)

        advanceUntilIdle()
        val s = vm.state.value
        assertFalse(s.computerThinking)
        assertEquals(2, 9 - s.game.board.emptyCells.size)
        assertEquals(Player.X, s.game.toMove)
    }

    @Test
    fun `computer opens when it is its turn to start`() = runTest(dispatcher) {
        val vm = vm()
        // Human plays O; X starts round one, so the computer moves first.
        vm.startMatch(MatchConfig(mode = Mode.VS_COMPUTER, difficulty = Difficulty.EASY, humanMark = Player.O))
        advanceUntilIdle()
        assertEquals(1, 9 - vm.state.value.game.board.emptyCells.size)
        assertEquals(Player.O, vm.state.value.game.toMove)
    }

    @Test
    fun `reset score clears the tally`() = runTest(dispatcher) {
        val vm = vm()
        vm.startMatch(MatchConfig(mode = Mode.TWO_PLAYER))
        listOf(0, 3, 1, 4, 2).forEach(vm::tap)
        vm.resetScore()
        assertEquals(Score(), vm.state.value.score)
    }
}
