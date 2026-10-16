# AGENTS.md

易經卜卦 Android app (一卜卦 / Yibokgua). Kotlin + Jetpack Compose, 4-module Gradle build. No CI, no existing instruction files.

## Build & verify
- JDK 17. Android SDK path comes from the gitignored `local.properties` (`sdk.dir=/opt/android_sdk`); recreate it if a fresh clone lacks it.
- `gradle.properties` sets `android.aapt2FromMavenOverride=/usr/bin/aapt2`. Builds fail without this system aapt2 — do not remove it.
- Commands:
  - `./gradlew :app:compileDebugKotlin` — fastest compile check
  - `./gradlew :app:testDebugUnitTest` — all unit tests
  - `./gradlew :app:testDebugUnitTest --tests "com.masteryi.yibokgua.DivinationEngineTest"` — one class
  - `./gradlew :app:assembleDebug` — debug APK
- No lint / detekt / ktlint / spotless configured. Verify with compile + unit tests; do not invent tooling.
- Unit tests live only in `app/src/test` (JUnit 4). `:core`, `:engine`, `:data` have no test sources — engine/domain logic is exercised from the app module.
- Test method names contain CJK (e.g. `fun of_2025春節()`); keep sources UTF-8 (`-Dfile.encoding=UTF-8` is set in `gradle.properties`).

## Module boundaries
One-way deps; keep domain logic in its module, not in `:app`:
- `:core` — pure models only (`Hexagram`, `Trigram`, `Yao`, `DivinationResult`, `LunarDate`, `TiYongAnalysis`, `EightPalace`). No project deps.
- `:engine` — algorithms (`DivinationEngine`, `TiYongAnalyzer`, `ChineseStrokeCounter`). Depends on `:core`.
- `:data` — repositories (`HexagramRepository` = 64 hexagrams, `TrigramInfoRepository`). Depends on `:core`.
- `:app` — Compose UI, ViewModels, settings, export. Depends on all three.
- No DI framework: repositories are plain classes instantiated directly inside ViewModels.
- `DivinationEngine` / `TiYongAnalyzer` take a `findHexagramByBinary: (String) -> Hexagram`; callers inject `HexagramRepository::getHexagramByBinary`. This is what keeps `:engine` free of `:data`.

## Domain conventions (easy to get wrong)
- `Hexagram.binaryCode` = 6 chars, `1`=陽 `0`=陰, ordered **lower trigram (3 bits) first, then upper** (下卦在前).
- Trigram numbers are 先天八卦: 乾1 兌2 離3 震4 巽5 坎6 艮7 坤8.
- Modulo is zero→max: `mod8` maps 0→8 (坤), `mod6` maps 0→6 (上爻).
- 時間起卦有兩版：`calculateTimeDivination`（國曆：年+月+日 / 時+分，非傳統）與 `calculateLunarTimeDivination`（農曆：年支+月+日[+時支]，傳統梅花）。`LunarDate.yearBranchNumber` 與 `hourBranchNumber(hour)` 提供 1–12 地支序（子1…亥12）。
- `rollSingleYao` 為傳統三錢法：每枚銅錢 3/2，三枚和 6/7/8/9 → 老陰/少陽/少陰/老陽，精確機率 1/8、3/8、3/8、1/8（12.5/37.5/37.5/12.5%）；`DivinationEngineTest` 以固定種子斷言。
- `ChineseStrokeCounter` 以 Unihan `kTotalStrokes`（來源 unicode-org/unicodetools，內建於 `engine/src/main/resources/unihan_stroke_counts.txt`）為準，**逐碼位採該字本身之繁體筆畫**（「請」U+8ACB=15 vs「请」U+8BF7=10）。涵蓋 BMP 漢字近 100%；未涵蓋之罕用字（多為擴充 B 區以上）回傳 `FALLBACK_STROKES = 6`，`hasData(cp)` 可判別；非漢字回傳 0。改動資料後以 `ChineseStrokeCounterTest` 驗證。
- Tests assume `HexagramRepository` holds exactly 64 unique hexagrams.

## State & persistence
- DataStore only, never SharedPreferences. Two stores: `yibokgua_settings` (`SettingsRepository`) and `yibokgua_reader` (`ReaderSettingsRepository`).
- `DivinationViewModel.historyList` is **in-memory only** — lost on process death. (README overstates this as persisted history.)
- ViewModels via `by viewModels()`: `DivinationViewModel` is a plain `ViewModel`; `SettingsViewModel` / `ReaderViewModel` are `AndroidViewModel`.
- Reader imports `.txt` via SAF `OpenDocument` and auto-detects BOM / UTF-8 / UTF-16 / GB18030.

## UI conventions
- All user-facing strings and code comments are Traditional Chinese (zh-TW). Match it; do not add English UI strings.
- Compose only — no XML layouts. XML is limited to the theme (`AppTheme`), manifest, and drawables.
- Global UI knobs via CompositionLocals in `ui/theme/CompositionLocals.kt`: `LocalBorderWidth`, `LocalKeyboardScale`, driven by settings.
- `MainActivity` tab indices: 0 首頁, 1 書籍, 2 64卦庫, 3 歷史, 4 設定, 5 起卦 (reached from Home only, no nav item). Keep indices contiguous — `ZenPageTransition` uses them for slide direction.
- Tabs are defined twice: `NavigationBarItems` (phone) and `NavigationRailItems` (wide). Update both, and wrap pages in `AdaptiveScreen` for large-screen width capping.
- Only `material-icons-core` is on the classpath (not `-extended`). Custom vectors (e.g. `BookIcon` in `ui/components/AppIcons.kt`) are built with `ImageVector.Builder` + `PathParser`; do not reference non-core `Icons.*`.

## Gotchas
- `app/release-keystore.jks` is committed but not referenced by any `signingConfig`; release builds are currently unsigned.
- Default branch is `master`. No CI, no branch protection.
