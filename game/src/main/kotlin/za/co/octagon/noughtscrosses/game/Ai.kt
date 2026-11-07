package za.co.octagon.noughtscrosses.game

import kotlin.random.Random

enum class Difficulty { EASY, MEDIUM, HARD }

/**
 * Computer opponent.
 * - Easy: a random empty cell.
 * - Medium: win if possible, otherwise block, otherwise random.
 * - Hard: perfect play via negamax with alpha-beta pruning (cannot be beaten).
 */
class Ai(private val random: Random = Random.Default) {

    fun chooseMove(board: Board, me: Player, difficulty: Difficulty): Int {
        val empty = board.emptyCells
        require(empty.isNotEmpty()) { "No moves left" }
        require(board.winner == null) { "The game is already won" }
        return when (difficulty) {
            Difficulty.EASY -> empty.random(random)
            Difficulty.MEDIUM -> winningMove(board, me)
                ?: winningMove(board, me.other)
                ?: empty.random(random)
            Difficulty.HARD -> bestMove(board, me)
        }
    }

    private fun winningMove(board: Board, player: Player): Int? =
        board.emptyCells.firstOrNull { board.place(it, player).winner == player }

    private fun bestMove(board: Board, me: Player): Int {
        var bestScore = Int.MIN_VALUE
        val best = mutableListOf<Int>()
        for (cell in board.emptyCells) {
            val score = -negamax(board.place(cell, me), me.other, depth = 1, -INF, INF)
            when {
                score > bestScore -> { bestScore = score; best.clear(); best += cell }
                score == bestScore -> best += cell
            }
        }
        // Several moves can be equally perfect; vary between them so games don't feel scripted.
        return best.random(random)
    }

    /** Score from the point of view of [toMove]. Faster wins and slower losses score higher. */
    private fun negamax(board: Board, toMove: Player, depth: Int, alphaIn: Int, beta: Int): Int {
        if (board.winner != null) return -(WIN - depth) // the previous mover just won
        if (board.isFull) return 0
        var alpha = alphaIn
        var best = -INF
        for (cell in board.emptyCells) {
            val score = -negamax(board.place(cell, toMove), toMove.other, depth + 1, -beta, -alpha)
            if (score > best) best = score
            if (score > alpha) alpha = score
            if (alpha >= beta) break
        }
        return best
    }

    private companion object {
        const val WIN = 100
        const val INF = 1_000
    }
}
