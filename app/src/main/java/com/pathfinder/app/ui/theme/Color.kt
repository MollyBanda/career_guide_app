package com.pathfinder.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

internal data class PathwisePalette(
    val ink: Color,
    val muted: Color,
    val blue: Color,
    val violet: Color,
    val border: Color,
    val page: Color,
    val surface: Color
)

internal val LightPathwisePalette = PathwisePalette(
    ink = Color(0xFF17233C), muted = Color(0xFF737F98), blue = Color(0xFF285FEA),
    violet = Color(0xFF514DDF), border = Color(0xFFE7E9F2), page = Color(0xFFF9F9FF),
    surface = Color.White
)

internal val DarkPathwisePalette = PathwisePalette(
    ink = Color(0xFFE5EAF5), muted = Color(0xFFADB8CC), blue = Color(0xFF86A7FF),
    violet = Color(0xFFB2ABFF), border = Color(0xFF38445A), page = Color(0xFF101522),
    surface = Color(0xFF1A2231)
)

internal val LocalPathwisePalette = staticCompositionLocalOf { LightPathwisePalette }

object AuthColors {
    val Navy: Color @Composable get() = LocalPathwisePalette.current.ink
    val Muted: Color @Composable get() = LocalPathwisePalette.current.muted
    val Blue: Color @Composable get() = LocalPathwisePalette.current.blue
    val Violet: Color @Composable get() = LocalPathwisePalette.current.violet
    val Border: Color @Composable get() = LocalPathwisePalette.current.border
    val Page: Color @Composable get() = LocalPathwisePalette.current.page
}

object DashboardColors {
    val Ink: Color @Composable get() = LocalPathwisePalette.current.ink
    val Muted: Color @Composable get() = LocalPathwisePalette.current.muted
    val Blue: Color @Composable get() = LocalPathwisePalette.current.blue
    val Line: Color @Composable get() = LocalPathwisePalette.current.border
    val Canvas: Color @Composable get() = LocalPathwisePalette.current.page
}
