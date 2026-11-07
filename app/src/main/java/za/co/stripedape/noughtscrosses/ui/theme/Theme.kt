package za.co.stripedape.noughtscrosses.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Colours for the two marks. Both clear WCAG 3:1 for graphics on their background. */
@Immutable
data class MarkColors(val x: Color, val o: Color, val grid: Color, val winLine: Color)

private val LightMarks = MarkColors(
    x = Color(0xFFD13F2A),
    o = Color(0xFF0B7F75),
    grid = Color(0xFF4B5563),
    winLine = Color(0xFF1D2A3A),
)

private val DarkMarks = MarkColors(
    x = Color(0xFFFF6B57),
    o = Color(0xFF3CD3C4),
    grid = Color(0xFFB5BFCC),
    winLine = Color(0xFFFFFFFF),
)

val LocalMarkColors = staticCompositionLocalOf { LightMarks }

private val LightScheme = lightColorScheme(
    primary = Color(0xFF1D2A3A),
    onPrimary = Color.White,
    secondary = Color(0xFF0B7F75),
    secondaryContainer = Color(0xFFD5EEEA),
    onSecondaryContainer = Color(0xFF06302C),
    background = Color(0xFFFAF5EF),
    onBackground = Color(0xFF1D2A3A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1D2A3A),
    surfaceVariant = Color(0xFFEFE7DD),
    onSurfaceVariant = Color(0xFF4A5563),
    // Containers (dialogs, cards) in warm neutrals instead of Material's default lavender.
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFCF8F3),
    surfaceContainer = Color(0xFFF7F1EA),
    surfaceContainerHigh = Color(0xFFFFFFFF),
    surfaceContainerHighest = Color(0xFFEFE7DD),
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFFDDE3EA),
    onPrimary = Color(0xFF121820),
    secondary = Color(0xFF3CD3C4),
    secondaryContainer = Color(0xFF14423D),
    onSecondaryContainer = Color(0xFFCFF5F0),
    background = Color(0xFF121820),
    onBackground = Color(0xFFE8ECF1),
    surface = Color(0xFF1C2430),
    onSurface = Color(0xFFE8ECF1),
    surfaceVariant = Color(0xFF263040),
    onSurfaceVariant = Color(0xFFB5BFCC),
    surfaceContainerLowest = Color(0xFF0E141B),
    surfaceContainerLow = Color(0xFF18202A),
    surfaceContainer = Color(0xFF1C2430),
    surfaceContainerHigh = Color(0xFF232C38),
    surfaceContainerHighest = Color(0xFF2A3442),
)

@Composable
fun NoughtsCrossesTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    androidx.compose.runtime.CompositionLocalProvider(
        LocalMarkColors provides if (dark) DarkMarks else LightMarks,
    ) {
        MaterialTheme(colorScheme = if (dark) DarkScheme else LightScheme, content = content)
    }
}
