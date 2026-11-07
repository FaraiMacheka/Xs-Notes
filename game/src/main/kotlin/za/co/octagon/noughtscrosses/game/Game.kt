package za.co.octagon.noughtscrosses.game

enum class Player {
    X, O;

    val other: Player get() = if (this == X) O else X
}

/** Immutable 3x3 board. Cells are indexed 0..8, row by row. */
class Board(private val cells: List<Player?> = List(SIZE) { null }) {

    init {
        require(cells.size == SIZE) { "Board needs $SIZE cells" }
    }

    operator fun get(index: Int): Player? = cells[index]

    fun isEmpty(index: Int): Boolean = cells[index] == null

    val emptyCells: List<Int> get() = cells.indices.filter { cells[it] == null }

    val isFull: Boolean get() = cells.none { it == null }

    val winningLine: List<Int>?
        get() = LINES.firstOrNull { (a, b, c) ->
            cells[a] != null && cells[a] == cells[b] && cells[a] == cells[c]
        }

    val winner: Player? get() = winningLine?.let { cells[it[0]] }

    fun place(index: Int, player: Player): Board {
        require(index in 0 until SIZE) { "Cell $index is off the board" }
        require(cells[index] == null) { "Cell $index is already taken" }
        return Board(cells.toMutableList().also { it[index] = player })
    }

    override fun equals(other: Any?): Boolean = other is Board && other.cells == cells
    override fun hashCode(): Int = cells.hashCode()
    override fun toString(): String =
        cells.chunked(3).joinToString("/") { row -> row.joinToString("") { it?.name ?: "." } }

    companion object {
        const val SIZE = 9
        val LINES: List<List<Int>> = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // rows
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // columns
            listOf(0, 4, 8), listOf(2, 4, 6),                  // diagonals
        )
    }
}

sealed interface Outcome {
    data class Win(val player: Player, val line: List<Int>) : Outcome
    data object Draw : Outcome
}

/** One round of noughts and crosses. Immutable: [play] returns the next state. */
data class Game(
    val board: Board = Board(),
    val toMove: Player = Player.X,
    val outcome: Outcome? = null,
) {
    val isOver: Boolean get() = outcome != null

    fun play(index: Int): Game {
        check(!isOver) { "The game is over" }
        val next = board.place(index, toMove)
        val line = next.winningLine
        val result = when {
            line != null -> Outcome.Win(toMove, line)
            next.isFull -> Outcome.Draw
            else -> null
        }
        return Game(next, toMove.other, result)
    }

    companion object {
        fun start(first: Player) = Game(toMove = first)
    }
}
