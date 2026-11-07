package za.co.stripedape.noughtscrosses.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class AiTest {

    private val ai = Ai(Random(42))

    /** Plays every possible opponent line against Hard and returns how many games the AI lost. */
    private fun lossesExploringAllLines(game: Game, aiPlayer: Player): Int {
        if (game.isOver) {
            val outcome = game.outcome
            return if (outcome is Outcome.Win && outcome.player != aiPlayer) 1 else 0
        }
        return if (game.toMove == aiPlayer) {
            val move = ai.chooseMove(game.board, aiPlayer, Difficulty.HARD)
            lossesExploringAllLines(game.play(move), aiPlayer)
        } else {
            game.board.emptyCells.sumOf { lossesExploringAllLines(game.play(it), aiPlayer) }
        }
    }

    @Test
    fun `hard never loses when moving first`() {
        assertEquals(0, lossesExploringAllLines(Game.start(Player.X), aiPlayer = Player.X))
        assertEquals(0, lossesExploringAllLines(Game.start(Player.O), aiPlayer = Player.O))
    }

    @Test
    fun `hard never loses when moving second`() {
        assertEquals(0, lossesExploringAllLines(Game.start(Player.X), aiPlayer = Player.O))
        assertEquals(0, lossesExploringAllLines(Game.start(Player.O), aiPlayer = Player.X))
    }

    @Test
    fun `hard takes an immediate win`() {
        // O O _ / X X _ / X _ _  with O to move: O must win at 2.
        val board = Board()
            .place(0, Player.O).place(1, Player.O)
            .place(3, Player.X).place(4, Player.X).place(6, Player.X)
        assertEquals(2, ai.chooseMove(board, Player.O, Difficulty.HARD))
    }

    @Test
    fun `medium always takes a winning move`() {
        repeat(50) {
            val board = Board().place(0, Player.X).place(1, Player.X).place(4, Player.O).place(8, Player.O)
            // X to move can win at 2 (top row); O also threatens nothing that matters here.
            assertEquals(2, Ai(Random(it)).chooseMove(board, Player.X, Difficulty.MEDIUM))
        }
    }

    @Test
    fun `medium always blocks a losing threat`() {
        repeat(50) {
            val board = Board().place(0, Player.X).place(1, Player.X).place(4, Player.O)
            assertEquals(2, Ai(Random(it)).chooseMove(board, Player.O, Difficulty.MEDIUM))
        }
    }

    @Test
    fun `medium prefers winning over blocking`() {
        repeat(50) {
            // X at 0,1 threatens 2; O at 3,4 threatens 5. O to move should win, not block.
            val board = Board()
                .place(0, Player.X).place(1, Player.X)
                .place(3, Player.O).place(4, Player.O)
            assertEquals(5, Ai(Random(it)).chooseMove(board, Player.O, Difficulty.MEDIUM))
        }
    }

    @Test
    fun `easy only ever picks empty cells`() {
        val board = Board().place(0, Player.X).place(4, Player.O).place(8, Player.X)
        repeat(200) {
            val move = Ai(Random(it)).chooseMove(board, Player.O, Difficulty.EASY)
            assertTrue(board.isEmpty(move))
        }
    }

    @Test
    fun `easy is not deterministic`() {
        val moves = (0 until 50).map { Ai(Random(it)).chooseMove(Board(), Player.X, Difficulty.EASY) }.toSet()
        assertNotEquals(1, moves.size)
    }
}
