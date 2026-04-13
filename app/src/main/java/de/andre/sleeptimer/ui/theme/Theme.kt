package de.andre.sleeptimer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Background / Surface ─────────────────────────────────────────────────────
val NightBlue    = Color(0xFF0D1117)
val SurfaceNight = Color(0xFF161B27)
val CardNight    = Color(0xFF1E2535)
val BorderNight  = Color(0xFF2A3347)

// ── Accents ───────────────────────────────────────────────────────────────────
val AccentPurple      = Color(0xFF7C86FF)
val AccentPurpleLight = Color(0xFF9EA8FF)
val AccentOrange      = Color(0xFFFFB347)
val AccentGreen       = Color(0xFF4CAF82)
val AccentRed         = Color(0xFFFF6B6B)

// ── Text ─────────────────────────────────────────────────────────────────────
val TextPrimary   = Color(0xFFE8EAF6)
val TextSecondary = Color(0xFF9098B3)
val TextMuted     = Color(0xFF5A6480)

private val DarkColorScheme = darkColorScheme(
    primary             = AccentPurple,
    onPrimary           = Color.White,
    primaryContainer    = Color(0xFF2C3166),
    onPrimaryContainer  = AccentPurpleLight,
    secondary           = AccentOrange,
    onSecondary         = Color.Black,
    secondaryContainer  = Color(0xFF3D2E10),
    onSecondaryContainer = AccentOrange,
    tertiary            = AccentGreen,
    background          = NightBlue,
    onBackground        = TextPrimary,
    surface             = SurfaceNight,
    onSurface           = TextPrimary,
    surfaceVariant      = CardNight,
    onSurfaceVariant    = TextSecondary,
    outline             = BorderNight,
    error               = AccentRed,
    onError             = Color.White
)

@Composable
fun SleepTimerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
