package com.masteryi.yibokgua.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography as MaterialTypography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.masteryi.yibokgua.settings.ThemeMode

/**
 * 全域圓角 Tokens：對齊墨紙系 [YiShape]。
 */
val YibokguaShapes = Shapes(
    extraSmall = YiShape.tiny,
    small = YiShape.small,
    medium = YiShape.medium,
    large = YiShape.large,
    extraLarge = YiShape.large
)

/**
 * 將墨紙色票映射到 Material3 角色。
 *
 * primary 保持墨色而非硃砂紅，避免全域元件誤用強調色；
 * 硃砂紅僅由首頁以 [YiColors] 顯式取用。
 */
private fun yiColorScheme(palette: YiPalette, dark: Boolean) = if (dark) {
    darkColorScheme(
        primary = palette.ink,
        onPrimary = palette.paper,
        primaryContainer = palette.paperMuted,
        onPrimaryContainer = palette.ink,
        secondary = palette.inkSecondary,
        onSecondary = palette.paper,
        secondaryContainer = palette.paperMuted,
        onSecondaryContainer = palette.ink,
        tertiary = palette.inkTertiary,
        onTertiary = palette.paper,
        tertiaryContainer = palette.paperMuted,
        onTertiaryContainer = palette.ink,
        background = palette.paper,
        onBackground = palette.ink,
        surface = palette.paperElevated,
        onSurface = palette.ink,
        surfaceVariant = palette.paperMuted,
        onSurfaceVariant = palette.inkSecondary,
        surfaceTint = palette.ink,
        inverseSurface = palette.ink,
        inverseOnSurface = palette.paper,
        inversePrimary = palette.paper,
        outline = palette.outline,
        outlineVariant = palette.outlineVariant,
        scrim = palette.ink.copy(alpha = 0.6f),
        error = palette.cinnabar,
        onError = palette.paper,
        errorContainer = palette.cinnabarContainer,
        onErrorContainer = palette.ink
    )
} else {
    lightColorScheme(
        primary = palette.ink,
        onPrimary = palette.paper,
        primaryContainer = palette.paperMuted,
        onPrimaryContainer = palette.ink,
        secondary = palette.inkSecondary,
        onSecondary = palette.paper,
        secondaryContainer = palette.paperMuted,
        onSecondaryContainer = palette.ink,
        tertiary = palette.inkTertiary,
        onTertiary = palette.paper,
        tertiaryContainer = palette.paperMuted,
        onTertiaryContainer = palette.ink,
        background = palette.paper,
        onBackground = palette.ink,
        surface = palette.paperElevated,
        onSurface = palette.ink,
        surfaceVariant = palette.paperMuted,
        onSurfaceVariant = palette.inkSecondary,
        surfaceTint = palette.ink,
        inverseSurface = palette.ink,
        inverseOnSurface = palette.paper,
        inversePrimary = palette.paper,
        outline = palette.outline,
        outlineVariant = palette.outlineVariant,
        scrim = palette.ink.copy(alpha = 0.6f),
        error = palette.cinnabar,
        onError = palette.paper,
        errorContainer = palette.cinnabarContainer,
        onErrorContainer = palette.ink
    )
}

@Composable
fun YibokguaTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    fontSizeScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val palette = remember(darkTheme) {
        if (darkTheme) YiDarkPalette else YiLightPalette
    }
    val colorScheme = remember(darkTheme) {
        yiColorScheme(palette, darkTheme)
    }
    val scaledTypography = remember(fontSizeScale) { yibokguaTypography(fontSizeScale) }
    val yiType = remember(fontSizeScale) { yiTypeStyles(fontSizeScale) }
    CompositionLocalProvider(
        LocalYiColors provides palette,
        LocalYiType provides yiType
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = scaledTypography,
            shapes = YibokguaShapes,
            content = content
        )
    }
}

/** 將標題類字族改回系統字型，保留目前字級倍率；供需避開子集字型缺字的畫面使用。 */
@Composable
fun systemFontTypography(): MaterialTypography {
    val base = MaterialTheme.typography
    fun TextStyle.system() = copy(fontFamily = FontFamily.Default)
    return base.copy(
        displayLarge = base.displayLarge.system(),
        displayMedium = base.displayMedium.system(),
        displaySmall = base.displaySmall.system(),
        headlineLarge = base.headlineLarge.system(),
        headlineMedium = base.headlineMedium.system(),
        headlineSmall = base.headlineSmall.system(),
        titleLarge = base.titleLarge.system(),
        titleMedium = base.titleMedium.system(),
        titleSmall = base.titleSmall.system()
    )
}

/** 標題類使用內嵌 Noto Serif TC；正文與標籤維持系統黑體，並依倍率縮放。 */
private fun yibokguaTypography(scale: Float): MaterialTypography {
    val base = MaterialTypography()
    fun TextStyle.heading() = copy(
        fontFamily = YiType.serifFamily,
        fontWeight = FontWeight.Medium,
        fontSize = fontSize * scale
    )
    fun TextStyle.body() = copy(
        fontFamily = YiType.sansFamily,
        fontSize = fontSize * scale
    )
    return MaterialTypography(
        displayLarge = base.displayLarge.heading(),
        displayMedium = base.displayMedium.heading(),
        displaySmall = base.displaySmall.heading(),
        headlineLarge = base.headlineLarge.heading(),
        headlineMedium = base.headlineMedium.heading(),
        headlineSmall = base.headlineSmall.heading(),
        titleLarge = base.titleLarge.heading(),
        titleMedium = base.titleMedium.heading(),
        titleSmall = base.titleSmall.heading(),
        bodyLarge = base.bodyLarge.body(),
        bodyMedium = base.bodyMedium.body(),
        bodySmall = base.bodySmall.body(),
        labelLarge = base.labelLarge.body(),
        labelMedium = base.labelMedium.body(),
        labelSmall = base.labelSmall.body()
    )
}
