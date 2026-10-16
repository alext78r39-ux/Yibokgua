# 一卜卦 (Yibokgua)

一個以 Kotlin + Jetpack Compose 開發的易經卜卦 Android 應用，支援梅花易數與多種起卦方式、卦象解析、歷史紀錄與詳情卡片導出。

## 專案架構

多模組 Gradle 專案：

| 模組 | 說明 |
|------|------|
| `:app` | Android 應用介面（Compose UI、ViewModel、畫面、設定、導出） |
| `:core` | 資料模型（`Hexagram`、`Trigram`、`Yao`、`DivinationResult`、`TiYongAnalysis` 等） |
| `:engine` | 起卦與解析邏輯（`DivinationEngine`、`TiYongAnalyzer`、筆畫計算） |
| `:data` | 卦象資料庫與儲存（`HexagramRepository` 64 卦、歷史紀錄） |

## 功能

### 五種起卦方式（「起卦」頁）

1. **手動搖卦** — 長按太極圖約 2 秒生成一爻，依序搖滿六爻成卦（傳統三錢法：老陰／少陽／少陰／老陽機率為 1/8、3/8、3/8、1/8）。
2. **快速起卦** — 一鍵直接生成六個爻成卦。
3. **時間起卦** — 可切換兩種取數方式：
   - **農曆（梅花易數，預設）** — 輸入年支、農曆月、農曆日與時支，依傳統取數：上卦＝(年支＋月＋日) mod 8，下卦＝(年支＋月＋日＋時支) mod 8，動爻＝同前 mod 6（地支序：子1〜亥12）。
   - **國曆** — 選擇國曆日期與時間（24 小時制），以年月日時分取數（非傳統梅花曆法，保留供對照）。
4. **類象加方位** — 輸入物象／觀察數與方位數取數起卦。
5. **字占起卦** — 輸入 1–10 個中文字，依字數、筆畫與分組取數成卦。

   - 多字分組採「前少後多」：偶數均分；奇數前少後多；上卦取前段總筆畫 mod 8，下卦取後段總筆畫 mod 8，動爻取全句總筆畫 mod 6。
   - 單字依邵子一字拆筆法（左右／上下／包圍結構拆筆）。
   - 筆畫數以 Unicode Unihan `kTotalStrokes` 為準，**逐字採該字本身之繁簡筆畫**（例：「請」=15、「请」=10），非漢字不計。
   - 「零取八（坤）／零取六（上爻）」由取模函式處理。

### 解析與顯示

- 本卦、變卦、動爻、六爻明細、體用生克、旺衰推斷。
- 首頁可輸入「所占之事」，起卦時自動帶入。
- 卦象查詢頁（六十四卦檢視）。
- 歷史紀錄：儲存起卦記錄，可查看詳情、導出詳情卡片。

### 詳情卡片導出

`DetailCardExport.kt` 以 Compose 離線渲染 PNG（設計寬 360dp），透過 `FileProvider` 以系統分享介面送出。

### 設定

主題（亮／暗／動態色彩）、字級、色彩預設、邊框寬度、鍵盤縮放；內建使用說明與關於資訊。

## 環境需求

- JDK 17
- Android SDK（`compileSdk = 35`，`minSdk = 26`，`targetSdk = 35`）
- Gradle 8.13（含 wrapper）

## 建置

```bash
# 產生除錯 APK
./gradlew :app:assembleDebug

# 編譯
./gradlew :app:compileDebugKotlin

# 執行單元測試
./gradlew :app:testDebugUnitTest

# 完整建置
./gradlew build
```

## 主要技術

- Kotlin 2.1.0
- Jetpack Compose（Material 3、BOM 2025.02.00）
- AndroidX Lifecycle / ViewModel / DataStore
- AGP 8.2.2
- JUnit 4（單元測試）

## 目錄重點

```
app/src/main/java/com/masteryi/yibokgua/
├── MainActivity.kt              # 主畫面、底部／側欄導覽
├── viewmodel/                   # DivinationViewModel 等
├── settings/                    # SettingsRepository / SettingsViewModel
├── ui/
│   ├── screen/
│   │   ├── home/                # HomeScreen, HistoryScreen, SettingsScreen
│   │   ├── input/               # DivinationInputScreen（五種起卦介面）
│   │   └── query/               # HexagramQueryScreen（卦象查詢）
│   ├── export/DetailCardExport.kt
│   └── components/, theme/
core/src/main/java/com/masteryi/core/model/iching/
engine/src/main/java/com/masteryi/engine/iching/
data/src/main/java/com/masteryi/data/repository/
```
