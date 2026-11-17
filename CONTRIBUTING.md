# 參與開發｜易卜卦

謝謝你願意參與易卜卦。這份文件說明如何把專案跑起來、如何驗證修改，以及送出 PR 前應該注意的事。

本專案以 **繁體中文（zh-TW）** 作為所有使用者字串與程式碼註解的語言，請沿用。

---

## 1. 環境需求

| 項目 | 版本 |
|---|---|
| JDK | 17 |
| Gradle | 8.13（含 wrapper，不需另外安裝） |
| Android SDK | `compileSdk = 35`、`targetSdk = 35`、`minSdk = 26` |
| Kotlin | 2.1.0 |
| AGP | 8.2.2 |

### 本機 SDK 路徑

`local.properties` 已被 `.gitignore` 排除。Clone 後請自行建立：

```properties
sdk.dir=/path/to/your/Android/Sdk
```

Windows 範例（注意反斜線要跳脫）：

```properties
sdk.dir=C\:\\Users\\<你的使用者名稱>\\AppData\\Local\\Android\\Sdk
```

### aapt2

`gradle.properties` **不設定** `android.aapt2FromMavenOverride`，由 AGP 自動使用 Maven 提供的、與平台相符的 aapt2（Windows 上是 `aapt2.exe`）。因此 clone 後可直接同步／建置，不需任何額外設定。

若你的本機 `gradle.properties` 仍有舊的 `android.aapt2FromMavenOverride=/usr/bin/aapt2`（或其他寫死的絕對路徑），請直接移除該行——它會讓 Android Studio 同步與 Gradle 建置在 Windows 上失敗。請勿把本機專用的 aapt2 路徑提交進版控。

---

## 2. 建置與驗證

```powershell
# 最快編譯檢查
.\gradlew.bat :app:compileDebugKotlin

# 全部單元測試（目前 14 個類別 / 83 個測試）
.\gradlew.bat :app:testDebugUnitTest

# 只跑單一測試類別（測試方法名稱含中文，請保持 UTF-8）
.\gradlew.bat :app:testDebugUnitTest --tests "com.masteryi.yibokgua.DivinationEngineTest"

# 產生除錯 APK
.\gradlew.bat :app:assembleDebug
```

專案**沒有** lint／detekt／ktlint／spotless。請勿自行引入工具；以「編譯 + 單元測試通過」作為驗證標準。

---

## 3. 程式碼結構與慣例

詳細的網域慣例、模組邊界與 UI 慣例請見 [`AGENTS.md`](AGENTS.md)，以下是最常踩到的幾條：

- **模組依賴是單向的**：`:core` → `:engine` / `:data` → `:app`。領域邏輯不要寫進 `:app`。
- `Hexagram.binaryCode` 是 6 字元，`1`=陽、`0`=陰，**下卦在前**。
- 先天八卦序：乾1 兌2 離3 震4 巽5 坎6 艮7 坤8。取模零取最大值：`mod8` 的 0 → 8（坤），`mod6` 的 0 → 6（上爻）。
- 三錢法機率必須是 1/8、3/8、3/8、1/8；改動後用 `DivinationEngineTest` 驗證。
- 筆畫以 Unihan `kTotalStrokes` 為準，逐碼位取字本身筆畫（「請」15 vs「请」10）。
- **Compose only**，XML 僅限 theme、manifest、drawables 與 appwidget provider。
- 符號（如 ☰、䷀）必須指定 `fontFamily = YiType.symbolFamily`，子集字型不含這些碼位。
- 狀態收集一律用 `collectAsStateWithLifecycle()`。
- 持久化只用 DataStore，**不要用 SharedPreferences**。
- 分頁索引請保持連續（0 首頁、1 書籍、2 64卦庫、3 歷史、4 設定、5 六壬、6 起卦），`ZenPageTransition` 依賴它判斷滑動方向。

---

## 4. 測試

- 單元測試目前只存在於 `app/src/test`（JUnit 4）；`:core`、`:engine`、`:data` 尚無測試來源。
- 引擎／領域邏輯是從 app 模組測試的，新增算法時請在 `app/src/test` 補上對應測試。
- 測試方法名稱可含中文（例如 `fun of_2025春節()`），請確保檔案維持 UTF-8。

---

## 5. Commit 與 Pull Request

1. 從 `master` 建立分支，例如 `feat/widget-hint` 或 `fix/stroke-fallback`。
2. 每個 commit 聚焦一件事，訊息用繁體中文寫清楚「改了什麼、為什麼」。
3. 送 PR 前請確認：
   - `.\gradlew.bat :app:compileDebugKotlin :app:testDebugUnitTest` 通過；
   - 沒有把 `local.properties` 或本機 SDK 路徑提交進去；
   - UI 字串與註解皆是繁體中文；
   - 若有使用者可見的變化，更新 `CHANGELOG.md` 的 `[Unreleased]`。
4. PR 描述請填寫模板中的「變更內容／驗證方式／影響範圍」。
5. CI 會在 PR 上執行編譯、83 個單元測試並上傳除錯 APK；請確保通過後再請求審查。

目前專案**沒有分支保護**，審查主要靠人工，感謝你的耐心。

---

## 6. 回報問題

請使用 GitHub issue 模板：

- **Bug 回報**：機型／Android 版本、重現步驟、預期與實際行為、是否為首次使用導覽後才出現。
- **功能建議**：使用情境優先於實作方式——請描述「你想完成什麼」，而不是只寫「加一個按鈕」。

安全性問題請勿公開 issue，直接聯絡維護者。

---

## 7. 授權

送出貢獻即表示你同意該貢獻以專案的 [MIT License](LICENSE) 授權。
