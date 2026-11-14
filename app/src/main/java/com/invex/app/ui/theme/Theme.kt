package com.invex.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Light only, no dynamic color, so the Figma palette is never overridden by the device wallpaper.
// Built on every call because the accent (primary) depends on who is logged in (orange or blue).
private fun invexColorScheme() = lightColorScheme(
    primary = Orange,
    onPrimary = SoftWhite,
    background = MobileBackground,
    onBackground = IconColor,
    surface = HoverSelect,
    onSurface = IconColor,
    outline = OutlineShadow,
)

@Composable
fun InvexTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = invexColorScheme(),
        typography = Typography,
        content = content,
    )
}
