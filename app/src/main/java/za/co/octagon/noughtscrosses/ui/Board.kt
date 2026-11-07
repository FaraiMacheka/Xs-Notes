package za.co.octagon.noughtscrosses.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import za.co.octagon.noughtscrosses.game.Board
import za.co.octagon.noughtscrosses.game.Player
import za.co.octagon.noughtscrosses.ui.theme.LocalMarkColors

/**
 * The 3x3 board. Each cell is its own clickable element with a spoken description
 * ("Row 2, column 3, empty") so TalkBack users can play.
 */
@Composable
fun BoardView(
    board: Board,
    winningLine: List<Int>?,
    enabled: Boolean,
    onCellTap: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalMarkColors.current
    Box(modifier.fillMaxWidth().aspectRatio(1f)) {
        Canvas(Modifier.fillMaxSize()) { drawGrid(colors.grid) }

        Column(Modifier.fillMaxSize()) {
            for (row in 0 until 3) {
                Row(Modifier.weight(1f).fillMaxWidth()) {
                    for (col in 0 until 3) {
                        val index = row * 3 + col
                        val mark = board[index]
                        val description = "Row ${row + 1}, column ${col + 1}, " +
                            (mark?.name ?: "empty")
                        Box(
                            Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .semantics { contentDescription = description }
                                .clickable(
                                    enabled = enabled && mark == null,
                                    role = Role.Button,
                                    onClickLabel = "Place mark",
                                ) { onCellTap(index) }
                                .padding(18.dp),
                        ) {
                            if (mark != null) {
                                // Keyed on the mark so the draw-in animation runs once when it is placed.
                                androidx.compose.runtime.key(index, mark) { Mark(mark) }
                            }
                        }
                    }
                }
            }
        }

        if (winningLine != null) {
            WinLine(winningLine)
        }
    }
}

@Composable
private fun Mark(player: Player) {
    val colors = LocalMarkColors.current
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) { progress.animateTo(1f, tween(durationMillis = 220)) }
    Canvas(Modifier.fillMaxSize()) {
        val stroke = size.minDimension * 0.13f
        val p = progress.value
        when (player) {
            Player.X -> {
                // First stroke draws in over the first half, the second over the rest.
                val a = (p * 2f).coerceAtMost(1f)
                val b = ((p - 0.5f) * 2f).coerceIn(0f, 1f)
                drawLine(colors.x, Offset.Zero, Offset(size.width * a, size.height * a), stroke, StrokeCap.Round)
                if (b > 0f) {
                    drawLine(
                        colors.x,
                        Offset(size.width, 0f),
                        Offset(size.width * (1 - b), size.height * b),
                        stroke,
                        StrokeCap.Round,
                    )
                }
            }
            Player.O -> {
                val inset = stroke / 2
                drawArc(
                    color = colors.o,
                    startAngle = -90f,
                    sweepAngle = 360f * p,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - stroke, size.height - stroke),
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
        }
    }
}

@Composable
private fun WinLine(line: List<Int>) {
    val color = LocalMarkColors.current.winLine
    val progress = remember(line) { Animatable(0f) }
    LaunchedEffect(line) { progress.animateTo(1f, tween(durationMillis = 450)) }
    Canvas(Modifier.fillMaxSize()) {
        val cell = size.width / 3
        fun centre(i: Int) = Offset((i % 3) * cell + cell / 2, (i / 3) * cell + cell / 2)
        val start = centre(line.first())
        val end = centre(line.last())
        // Extend a little past the outer cells' centres.
        val dir = (end - start) / (end - start).getDistance()
        val from = start - dir * (cell * 0.35f)
        val to = end + dir * (cell * 0.35f)
        drawLine(
            color = color,
            start = from,
            end = from + (to - from) * progress.value,
            strokeWidth = cell * 0.08f,
            cap = StrokeCap.Round,
            alpha = 0.85f,
        )
    }
}

private fun DrawScope.drawGrid(color: androidx.compose.ui.graphics.Color) {
    val third = size.width / 3
    val stroke = size.width * 0.018f
    val pad = size.width * 0.03f
    for (i in 1..2) {
        val p = third * i
        // Opaque colour: semi-transparent lines would show darker squares where they cross.
        drawLine(color, Offset(p, pad), Offset(p, size.height - pad), stroke, StrokeCap.Round)
        drawLine(color, Offset(pad, p), Offset(size.width - pad, p), stroke, StrokeCap.Round)
    }
}
