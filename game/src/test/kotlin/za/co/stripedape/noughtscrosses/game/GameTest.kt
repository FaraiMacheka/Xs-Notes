package za.co.stripedape.noughtscrosses.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameTest {

    private fun Game.playAll(vararg cells: Int): Game = cells.fold(this) { g, c -> g.play(c) }

    @Test
    fun `every winning line wins for X`() {
        for (line in Board.LINES) {
            val others = (0..8).filter { it !in line }
            // X plays the line, O plays harmless cells that never complete a line of their own.
            var game = Game.start(Player.X)
            val oMoves = others.iterator()
            for ((index, cell) in line.withIndex()) {
                game = game.play(cell)
                if (index < 2) {
                    var o = oMoves.next()
                    while (!game.board.isEmpty(o)) o = oMoves.next()
                    game = game.play(o)
                }
            }
            val outcome = game.outcome
            assertTrue("line $line should win", outcome is Outcome.Win)
            outcome as Outcome.Win
            assertEquals(Player.X, outcome.player)
            assertEquals(line, outcome.line)
        }
    }

    @Test
    fun `every winning line wins for O`() {
        for (line in Board.LINES) {
            var board = Board()
            line.forEach { board = board.place(it, Player.O) }
            assertEquals(Player.O, board.winner)
            assertEquals(line, board.winningLine)
        }
    }

    @Test
    fun `full board with no line is a draw`() {
        // X O X / X O O / O X X
        val game = Game.start(Player.X).playAll(0, 1, 2, 4, 3, 5, 7, 6, 8)
        assertEquals(Outcome.Draw, game.outcome)
        assertTrue(game.isOver)
    }

    @Test
    fun `players alternate turns`() {
        val game = Game.start(Player.O)
        assertEquals(Player.O, game.toMove)
        val next = game.play(4)
        assertEquals(Player.X, next.toMove)
        assertEquals(Player.O, next.board[4])
    }

    @Test(expected = IllegalArgumentException::class)
    fun `occupied cell is rejected`() {
        Game.start(Player.X).play(4).play(4)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `out of range cell is rejected`() {
        Game.start(Player.X).play(9)
    }

    @Test(expected = IllegalStateException::class)
    fun `no moves after game ends`() {
        val won = Game.start(Player.X).playAll(0, 3, 1, 4, 2)
        assertTrue(won.isOver)
        won.play(8)
    }

    @Test
    fun `game in progress has no outcome`() {
        val game = Game.start(Player.X).playAll(0, 4)
        assertNull(game.outcome)
        assertFalse(game.isOver)
    }
}
