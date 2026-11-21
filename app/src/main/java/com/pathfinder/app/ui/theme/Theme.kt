package com.pathfinder.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import android.app.Activity

@Composable
fun PathwiseTheme(content: @Composable () -> Unit) {
    val darkTheme = isSystemInDarkTheme()
    val palette = if (darkTheme) DarkPathwisePalette else LightPathwisePalette
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = palette.blue,
            secondary = palette.violet,
            background = palette.page,
            surface = palette.surface,
            onPrimary = palette.ink,
            onSecondary = palette.ink,
            onBackground = palette.ink,
            onSurface = palette.ink
        )
    } else {
        lightColorScheme(
            primary = palette.blue,
            secondary = palette.violet,
            background = palette.page,
            surface = palette.surface,
            onSurface = palette.ink,
            onBackground = palette.ink
        )
    }
    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        window.statusBarColor = palette.page.toArgb()
        window.navigationBarColor = palette.page.toArgb()
        window.decorView.systemUiVisibility = if (darkTheme) 0 else
            (android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
    }
    CompositionLocalProvider(LocalPathwisePalette provides palette) {
        MaterialTheme(colorScheme = colorScheme, content = content)
    }
}
