package com.masteryi.yibokgua.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * 墨紙系色彩 Tokens。
 *
 * 硃砂紅使用紀律：全局僅允許出現在下列三處，其餘一律使用墨色系：
 * 1. 「起卦」按鈕底色
 * 2. 八卦卡選中框
 * 3. 首頁分隔線左端點
 *
 * 深色模式使用 [YiDarkPalette]；透過 [LocalYiColors] 由 [YibokguaTheme] 提供。
 */
@Immutable
data class YiPalette(
    /** 宣紙底：全局背景 */
    val paper: Color,
    /** 紙色提升：卡片／輸入框底 */
    val paperElevated: Color,
    /** 紙色內縮：次要區塊底 */
    val paperMuted: Color,
    /** 墨黑：主文字、太極陰 */
    val ink: Color,
    /** 次級文字 */
    val inkSecondary: Color,
    /** placeholder、提示 */
    val inkTertiary: Color,
    /** 硃砂紅：唯一強調色（使用紀律見檔頭） */
    val cinnabar: Color,
    /** 硃砂紅 12%：選中底、按壓態 */
    val cinnabarSoft: Color,
    /** 硃砂紅淡底：容器用（不直接大面積使用） */
    val cinnabarContainer: Color,
    /** 髮絲邊框：墨黑 40% */
    val hairline: Color,
    /** 極淡邊框／八卦環軌道：墨黑 15% */
    val hairlineFaint: Color,
    /** 實色框線：Material outline */
    val outline: Color,
    /** 極淡實色框線：Material outlineVariant */
    val outlineVariant: Color,
    /** 太極陽 */
    val yang: Color,
    /** 紙張頂部內高光（卡片實作細節） */
    val paperHighlight: Color
)

val YiLightPalette = YiPalette(
    paper = Color(0xFFF7F4ED),
    paperElevated = Color(0xFFFBFAF6),
    paperMuted = Color(0xFFF1EEE6),
    ink = Color(0xFF1C1B18),
    inkSecondary = Color(0xFF5C5A54),
    inkTertiary = Color(0xFF8A8780),
    cinnabar = Color(0xFFA63A2E),
    cinnabarSoft = Color(0x1FA63A2E),
    cinnabarContainer = Color(0xFFF1E0DC),
    hairline = Color(0x661C1B18),
    hairlineFaint = Color(0x261C1B18),
    outline = Color(0xFFB9B3A8),
    outlineVariant = Color(0xFFD9D3C7),
    yang = Color(0xFFFBFAF6),
    paperHighlight = Color(0x99FFFFFF)
)

val YiDarkPalette = YiPalette(
    paper = Color(0xFF141310),
    paperElevated = Color(0xFF1E1C18),
    paperMuted = Color(0xFF25221D),
    ink = Color(0xFFE7E2D8),
    inkSecondary = Color(0xFFB8B2A7),
    inkTertiary = Color(0xFF8A857B),
    cinnabar = Color(0xFFD4685A),
    cinnabarSoft = Color(0x33D4685A),
    cinnabarContainer = Color(0xFF3A2420),
    hairline = Color(0x66E7E2D8),
    hairlineFaint = Color(0x26E7E2D8),
    outline = Color(0xFF5C574E),
    outlineVariant = Color(0xFF3A3733),
    yang = Color(0xFF1E1C18),
    paperHighlight = Color(0x1AFFFFFF)
)

/** 由 [YibokguaTheme] 依明暗模式提供 */
val LocalYiColors = staticCompositionLocalOf { YiLightPalette }

/** 目前主題的墨紙色票；僅可在 Composable 或 Composable 預設參數中讀取。 */
val YiColors: YiPalette
    @Composable get() = LocalYiColors.current
