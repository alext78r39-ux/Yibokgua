# Yibokgua（易卜卦）

以梅花易數為核心的易經起卦 Android App。採用 Jetpack Compose 建置現代化 UI，內建完整 64 卦資料庫（卦辭、彖傳、大象傳、六爻爻辭、分爻小象傳與歷代注解），並提供「體用生克」分析引擎，自動判斷吉凶。

## 功能特色

- **四種起卦方式**
  - 自動搖卦：長按太極圖 2 秒生成一爻，依序搖滿六爻成卦
  - 手動搖卦：以老陰 6・少陽 7・少陰 8・老陽 9 依序指定六爻
  - 時間起卦：依梅花易數以年月日與時分起卦
  - 類象加方位起卦：以物象數與方位數起卦
- **體用生克分析**：自動判定體卦／用卦、互卦、變卦，輸出「體用比和、體生用、用生體、體克用、用克體」五種關係與吉凶斷語
- **64 卦資料庫**：完整收錄周易本經卦辭、彖傳、大象傳、六爻爻辭、分爻小象傳與歷代注解（王弼、朱熹、孔穎達等）
- **卦象詳情**：以 Unicode 易經六十四卦符號顯示完整卦象，可查詢梅花易數屬性與體用生克情境
- **自適應 UI**：窄螢幕使用底部 NavigationBar，寬螢幕（平板／大視窗）自動切換側邊 NavigationRail
- **主題**：朱砂紅／玄金／宣紙米白高對比色系，支援系統深色模式

## 技術棧

- Kotlin、Android Jetpack Compose（Material 3）
- MVVM（`ViewModel` + `StateFlow`）
- 多模組架構：`core`（模型）／`engine`（起卦與分析引擎）／`data`（資料庫）／`app`（UI）
- 單元測試：JUnit 4

## 專案結構

```
├── app/      # Android App（Compose UI、ViewModel、測試）
├── core/     # 核心模型：爻、八卦、64 卦、體用生克資料
├── data/     # 64 卦資料庫（記憶體 Repository）
├── engine/   # 起卦引擎與體用生克分析器
└── settings.gradle.kts
```

### 核心邏輯

- `engine/.../DivinationEngine.kt` — 時間起卦、自動／手動搖卦、類象方位起卦、六爻成卦
- `engine/.../TiYongAnalyzer.kt` — 體用生克分析（互卦、變卦、體黨／用黨）
- `data/.../HexagramRepository.kt` — 64 卦完整文本資料
- `core/.../model/iching/` — `Yao`、`Trigram`、`Hexagram`、`DivinationResult`、`TiYongAnalysis` 等模型

## 建置與執行

環境需求：

- Android Studio（建議最新穩定版）
- JDK 17+
- Android SDK 35（`compileSdk = 35`，`minSdk = 26`）

```bash
# 執行單元測試
./gradlew :app:testDebugUnitTest

# 建置 Debug APK
./gradlew :app:assembleDebug
```

> 注意：`gradle.properties` 預設使用 AGP 內建 AAPT2，若曾在 Linux 環境自訂 `android.aapt2FromMavenOverride` 請先移除，避免在 Windows 上無法建置。

## 測試

`app/src/test/java/com/masteryi/yibokgua/` 內含 19 個單元測試：

- `DivinationEngineTest` — 起卦引擎（時間起卦、機率分佈、變爻轉換、64 卦唯一性）
- `TiYongAnalyzerTest` — 體用生克分析
- `HexagramDataTest` — 64 卦資料完整性

## License

本專案僅供學習與娛樂用途。易經經文內容屬公共領域。
