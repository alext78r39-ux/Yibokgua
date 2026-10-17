package com.masteryi.yibokgua.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.masteryi.yibokgua.settings.ColorPreset
import com.masteryi.yibokgua.settings.ThemeMode

/**
 * 圓角 Tokens：全面加大圓角，卡片與對話窗一律取 [Shapes.extraLarge] = 24dp，
 * 讓版面呈現柔和、留白的禪意感。
 */
val YibokguaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

/**
 * 建立低對比淺色配色。
 * 各主題只傳入「帶色」角色，宣紙／墨色／框線等中性色由這裡統一壓低對比，
 * 確保五套主題的沉靜基調一致。
 */
private fun zenLightScheme(
    primary: Color,
    onPrimary: Color,
    primaryContainer: Color,
    onPrimaryContainer: Color,
    secondary: Color,
    onSecondary: Color,
    secondaryContainer: Color,
    onSecondaryContainer: Color
) = lightColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,
    secondary = secondary,
    onSecondary = onSecondary,
    secondaryContainer = secondaryContainer,
    onSecondaryContainer = onSecondaryContainer,
    tertiary = secondary,
    onTertiary = onSecondary,
    tertiaryContainer = secondaryContainer,
    onTertiaryContainer = onSecondaryContainer,
    background = YiPaper,
    onBackground = YiOnPaper,
    surface = YiPaper,
    onSurface = YiOnPaper,
    surfaceVariant = YiSurfaceVariantLight,
    onSurfaceVariant = YiOnSurfaceVariantLight,
    outline = YiOutlineLight,
    outlineVariant = YiOutlineVariantLight,
    scrim = ZenScrim,
    error = ZenErrorLight,
    onError = OnZenErrorLight,
    errorContainer = ZenErrorContainerLight,
    onErrorContainer = OnZenErrorContainerLight
)

/** 建立低對比暗色配色，語意與 [zenLightScheme] 相同 */
private fun zenDarkScheme(
    primary: Color,
    onPrimary: Color,
    primaryContainer: Color,
    onPrimaryContainer: Color,
    secondary: Color,
    onSecondary: Color,
    secondaryContainer: Color,
    onSecondaryContainer: Color
) = darkColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,
    secondary = secondary,
    onSecondary = onSecondary,
    secondaryContainer = secondaryContainer,
    onSecondaryContainer = onSecondaryContainer,
    tertiary = secondary,
    onTertiary = onSecondary,
    tertiaryContainer = secondaryContainer,
    onTertiaryContainer = onSecondaryContainer,
    background = YiPaperDark,
    onBackground = YiOnPaperDark,
    surface = YiPaperDark,
    onSurface = YiOnPaperDark,
    surfaceVariant = YiSurfaceVariantDark,
    onSurfaceVariant = YiOnSurfaceVariantDark,
    outline = YiOutlineDark,
    outlineVariant = YiOutlineVariantDark,
    scrim = ZenScrim,
    error = ZenErrorDark,
    onError = OnZenErrorDark,
    errorContainer = ZenErrorContainerDark,
    onErrorContainer = OnZenErrorContainerDark
)

// ==================== 朱砂金（預設） ====================
private val LightColorScheme = zenLightScheme(
    primary = YiRedLight, onPrimary = ZenOnAccentLight,
    primaryContainer = YiRedContainer, onPrimaryContainer = OnYiRedContainer,
    secondary = YiGoldLight, onSecondary = ZenOnAccentLight,
    secondaryContainer = YiGoldContainer, onSecondaryContainer = OnYiGoldContainer
)

private val DarkColorScheme = zenDarkScheme(
    primary = YiRedDark, onPrimary = OnYiRedDark,
    primaryContainer = YiDarkRedContainer, onPrimaryContainer = YiRedContainer,
    secondary = YiGoldDark, onSecondary = OnYiGoldDark,
    secondaryContainer = YiGoldContainerDark, onSecondaryContainer = YiGoldContainer
)

// ==================== 青瓷藍 ====================
private val BlueLightScheme = zenLightScheme(
    primary = BluePrimaryLight, onPrimary = ZenOnAccentLight,
    primaryContainer = BluePrimaryContainerLight, onPrimaryContainer = OnBluePrimaryContainerLight,
    secondary = BlueSecondaryLight, onSecondary = ZenOnAccentLight,
    secondaryContainer = BlueSecondaryContainerLight, onSecondaryContainer = OnBlueSecondaryContainerLight
)

private val BlueDarkScheme = zenDarkScheme(
    primary = BluePrimaryDark, onPrimary = OnBluePrimaryDark,
    primaryContainer = BlueDarkPrimaryContainerLight, onPrimaryContainer = BluePrimaryContainerLight,
    secondary = BlueSecondaryDark, onSecondary = OnBlueSecondaryDark,
    secondaryContainer = BlueSecondaryContainerDark, onSecondaryContainer = BlueSecondaryContainerLight
)

// ==================== 翡翠綠 ====================
private val GreenLightScheme = zenLightScheme(
    primary = GreenPrimaryLight, onPrimary = ZenOnAccentLight,
    primaryContainer = GreenPrimaryContainerLight, onPrimaryContainer = OnGreenPrimaryContainerLight,
    secondary = GreenSecondaryLight, onSecondary = ZenOnAccentLight,
    secondaryContainer = GreenSecondaryContainerLight, onSecondaryContainer = OnGreenSecondaryContainerLight
)

private val GreenDarkScheme = zenDarkScheme(
    primary = GreenPrimaryDark, onPrimary = OnGreenPrimaryDark,
    primaryContainer = GreenDarkPrimaryContainerLight, onPrimaryContainer = GreenPrimaryContainerLight,
    secondary = GreenSecondaryDark, onSecondary = OnGreenSecondaryDark,
    secondaryContainer = GreenSecondaryContainerDark, onSecondaryContainer = GreenSecondaryContainerLight
)

// ==================== 紫水晶 ====================
private val PurpleLightScheme = zenLightScheme(
    primary = PurplePrimaryLight, onPrimary = ZenOnAccentLight,
    primaryContainer = PurplePrimaryContainerLight, onPrimaryContainer = OnPurplePrimaryContainerLight,
    secondary = PurpleSecondaryLight, onSecondary = ZenOnAccentLight,
    secondaryContainer = PurpleSecondaryContainerLight, onSecondaryContainer = OnPurpleSecondaryContainerLight
)

private val PurpleDarkScheme = zenDarkScheme(
    primary = PurplePrimaryDark, onPrimary = OnPurplePrimaryDark,
    primaryContainer = PurpleDarkPrimaryContainerLight, onPrimaryContainer = PurplePrimaryContainerLight,
    secondary = PurpleSecondaryDark, onSecondary = OnPurpleSecondaryDark,
    secondaryContainer = PurpleSecondaryContainerDark, onSecondaryContainer = PurpleSecondaryContainerLight
)

// ==================== 琥珀橘 ====================
private val OrangeLightScheme = zenLightScheme(
    primary = OrangePrimaryLight, onPrimary = ZenOnAccentLight,
    primaryContainer = OrangePrimaryContainerLight, onPrimaryContainer = OnOrangePrimaryContainerLight,
    secondary = OrangeSecondaryLight, onSecondary = ZenOnAccentLight,
    secondaryContainer = OrangeSecondaryContainerLight, onSecondaryContainer = OnOrangeSecondaryContainerLight
)

private val OrangeDarkScheme = zenDarkScheme(
    primary = OrangePrimaryDark, onPrimary = OnOrangePrimaryDark,
    primaryContainer = OrangeDarkPrimaryContainerLight, onPrimaryContainer = OrangePrimaryContainerLight,
    secondary = OrangeSecondaryDark, onSecondary = OnOrangeSecondaryDark,
    secondaryContainer = OrangeSecondaryContainerDark, onSecondaryContainer = OrangeSecondaryContainerLight
)

private fun colorSchemeFor(preset: ColorPreset, dark: Boolean): androidx.compose.material3.ColorScheme = when (preset) {
    ColorPreset.DEFAULT -> if (dark) DarkColorScheme else LightColorScheme
    ColorPreset.BLUE -> if (dark) BlueDarkScheme else BlueLightScheme
    ColorPreset.GREEN -> if (dark) GreenDarkScheme else GreenLightScheme
    ColorPreset.PURPLE -> if (dark) PurpleDarkScheme else PurpleLightScheme
    ColorPreset.ORANGE -> if (dark) OrangeDarkScheme else OrangeLightScheme
}

@Composable
fun YibokguaTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    fontSizeScale: Float = 1.0f,
    colorPreset: ColorPreset = ColorPreset.DEFAULT,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val context = LocalContext.current
    val colorScheme = remember(colorPreset, darkTheme, dynamicColor, context) {
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            else -> colorSchemeFor(colorPreset, darkTheme)
        }
    }
    val scaledTypography = remember(fontSizeScale) { scaledTypography(fontSizeScale) }
    val motion = remember { ZenMotion() }
    CompositionLocalProvider(LocalMotion provides motion) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = scaledTypography,
            shapes = YibokguaShapes,
            content = content
        )
    }
}

/** 依倍率縮放所有字級 */
private fun scaledTypography(scale: Float) = androidx.compose.material3.Typography(
    displayLarge = Typography.displayLarge.copy(fontSize = Typography.displayLarge.fontSize * scale),
    displayMedium = Typography.displayMedium.copy(fontSize = Typography.displayMedium.fontSize * scale),
    displaySmall = Typography.displaySmall.copy(fontSize = Typography.displaySmall.fontSize * scale),
    headlineLarge = Typography.headlineLarge.copy(fontSize = Typography.headlineLarge.fontSize * scale),
    headlineMedium = Typography.headlineMedium.copy(fontSize = Typography.headlineMedium.fontSize * scale),
    headlineSmall = Typography.headlineSmall.copy(fontSize = Typography.headlineSmall.fontSize * scale),
    titleLarge = Typography.titleLarge.copy(fontSize = Typography.titleLarge.fontSize * scale),
    titleMedium = Typography.titleMedium.copy(fontSize = Typography.titleMedium.fontSize * scale),
    titleSmall = Typography.titleSmall.copy(fontSize = Typography.titleSmall.fontSize * scale),
    bodyLarge = Typography.bodyLarge.copy(fontSize = Typography.bodyLarge.fontSize * scale),
    bodyMedium = Typography.bodyMedium.copy(fontSize = Typography.bodyMedium.fontSize * scale),
    bodySmall = Typography.bodySmall.copy(fontSize = Typography.bodySmall.fontSize * scale),
    labelLarge = Typography.labelLarge.copy(fontSize = Typography.labelLarge.fontSize * scale),
    labelMedium = Typography.labelMedium.copy(fontSize = Typography.labelMedium.fontSize * scale),
    labelSmall = Typography.labelSmall.copy(fontSize = Typography.labelSmall.fontSize * scale)
)