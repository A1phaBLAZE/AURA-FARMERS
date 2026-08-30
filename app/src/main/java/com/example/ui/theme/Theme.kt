package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = KisanGreenPrimaryDark,
    onPrimary = Color(0xFF00390B),
    primaryContainer = KisanGreenContainerDark,
    onPrimaryContainer = Color(0xFFA6F5A6),
    secondary = KisanSaffronLight,
    onSecondary = Color(0xFF552000),
    secondaryContainer = Color(0xFF743000),
    onSecondaryContainer = Color(0xFFFFDBCF),
    tertiary = KisanGold,
    background = KisanBgDark,
    onBackground = Color(0xFFE2E3DF),
    surface = KisanSurfaceDark,
    onSurface = Color(0xFFE2E3DF),
    surfaceVariant = KisanSurfaceVariantDark,
    onSurfaceVariant = Color(0xFFC2CCC3),
    outline = Color(0xFF8C9388)
)

private val LightColorScheme = lightColorScheme(
    primary = KisanGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = KisanGreenContainer,
    onPrimaryContainer = KisanOnGreenContainer,
    secondary = KisanSaffron,
    onSecondary = Color.White,
    secondaryContainer = KisanSaffronContainer,
    onSecondaryContainer = Color(0xFF4E1600),
    tertiary = KisanGold,
    background = KisanBgLight,
    onBackground = Color(0xFF191C19),
    surface = KisanSurfaceLight,
    onSurface = Color(0xFF191C19),
    surfaceVariant = KisanSurfaceVariantLight,
    onSurfaceVariant = Color(0xFF424940),
    outline = KisanOutlineLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use intentional branded agriculture palette
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

