# 易卜卦（Yibokgua）— 開發者關係研究報告

> 資料快照：2026-11-18
> 資料來源：GitHub REST API、StackExchange API 2.3、Hacker News Algolia API、即時網路搜尋，以及專案本地文檔（`README.md`、`AGENTS.md`）。
> 本報告以開發者關係（DevRel）顧問觀點撰寫，量化數據均註明來源；無法取得的數據標記為 `No data available`。

---

## 0. 執行摘要

- **文檔已找到**：`README.md`（125 行，完整功能／架構／建置說明）與 `AGENTS.md`（模組邊界、網域慣例、UI 慣例、Gotchas）。因此不回報 `Unable to find docs`。
- 專案目前處於**「單人維護、尚無社群」階段**：GitHub 上 0 stars / 0 forks / 0 watchers / 0 issues / 0 PRs / 0 releases。
- 生態系本身很強（Kotlin、Compose 在 StackOverflow 有大量問答），但**專案本身的公開討論足跡為零**（HN 0 篇、SO 0 題、TechCrunch／Crunchbase 無資料）。
- 近期工程狀態良好：重設計完成、83 個單元測試全綠、debug APK 可產出，commit `8e2e096` 已推送 `origin/master`。

---

## 1. 文檔與專案現況

| 面向 | 內容 |
|---|---|
| 架構 | 4 模組單向依賴：`:core`（純模型）→ `:engine`（算法）／`:data`（64 卦 Repository）→ `:app`（Compose UI／ViewModel／設定／導出） |
| 主要功能 | 5 種起卦方式、體用生克解析、64 卦查詢、九宮六壬、書籍閱讀器（PDF／DOC／DOCX／MD／TXT）、2 款 Glance 桌面小工具、詳情卡片 PNG 導出 |
| 技術棧 | Kotlin 2.1.0、Compose Material 3（BOM 2025.02.00）、Glance 1.1.1、DataStore、AGP 8.2.2、Gradle 8.13、JDK 17、JUnit 4 |
| 文檔品質 | README 結構清晰；AGENTS.md 對網域慣例（binaryCode 下卦在前、mod8 零取八、先天八卦序、三錢法機率、字型符號缺字陷阱等）記錄得非常細緻，對新貢獻者價值很高 |

**文檔漂移（DevRel 應修正）**：`AGENTS.md` 註記「README 誇大了歷史紀錄為持久化」，但現行 README 第 38 行已正確寫成「暫存於記憶體（最多 200 筆，結束行程後清空）」。這類不一致會誤導貢獻者，建議統一。

**工程可信度**：本地驗證 `compileDebugKotlin + testDebugUnitTest + assembleDebug` 全部通過（14 個測試套件、83 個測試、0 失敗）。

---

## 2. GitHub 量化分析

| 指標 | Yibokgua | IBK（同作者） | 說明 |
|---|---:|---:|---|
| Repository ID | 1379067100 | 1421270358 | |
| 建立時間 | 2026-09-21 | 2026-10-15 | 專案約 2 個月大 |
| 最後 push | 2026-11-17 | 未取得 | 本次重設計已推送 |
| Stars | **0** | 0 | |
| Forks | **0** | 0 | |
| Watchers（API） | **0** | 0 | 註：現行 API 的 `watchers_count` 與 stars 同值；真實追蹤數看 `subscribers_count` |
| 提交 Issue 數 | **0** | 0 | `/issues?state=all` 回傳空陣列 |
| 關閉 Issue 數 | **0** | 0 | 搜尋 `is:issue is:closed` → `total_count=0` |
| 合併 PR 數 | **0** | 0 | 搜尋 `is:pr is:merged` → `total_count=0` |
| Releases | **0** | 0 | 無正式發布 |
| Open issues | 0 | 0 | |
| 貢獻者 | **1**（21 次貢獻） | — | 全為 `alext78r39-ux` |
| 語言 | Kotlin 490,527 bytes（100%） | Kotlin | |
| 授權 | MIT | 無 | IBK 缺少授權是風險 |
| 程式碼體積 | 325 KB | 201 KB | |

**commit 活躍度**（52 週 participation，最後 16 週）：`1,0,8,1,2,0,0,2,0,0,0,4,0,0,0,2` → 近期合計 **20 次提交**，且 `owner` 陣列與 `all` 完全相同，證實 **bus factor = 1**。

**專業解讀**：issue／PR 全為 0 不一定是壞事（代表沒有積壓），但同時代表**沒有任何外部回饋迴路**。對 DevRel 而言，這是從「個人專案」邁向「社群專案」的起點：缺少 issue template、CONTRIBUTING、changelog、release tag，使得潛在貢獻者無從切入。

---

## 3. StackOverflow 量化分析

### 3.1 標籤規模

| 標籤 | 問題數 | 相對 android |
|---|---:|---:|
| `android` | 1,413,683 | 100% |
| `android-studio` | 90,142 | 6.4% |
| `kotlin` | 97,579 | 6.9% |
| `android-jetpack-compose` | 14,272 | 1.0% |
| `android-glance` | 無資料（API 回傳空） | — |
| `Yibokgua` | 0（搜尋回傳空） | — |

### 3.2 全站活動量（StackOverflow 整體）

| 指標 | 數值 | 衍生比率 |
|---|---:|---:|
| 總問題數 | 24,245,291 | — |
| 總回答數 | 36,100,826 | 1.49 回答／問題 |
| 未解答問題 | 7,211,670 | 29.7% 未解答率 |
| 已接受答案 | 12,397,018 | 51.1% 接受率 |
| 總使用者 | 31,747,054 | — |
| 總投票數 | 216,305,270 | — |
| 問題頻率 | 2.52 題／分鐘 | — |
| 回答頻率 | 3.75 則／分鐘 | — |

### 3.3 `android-jetpack-compose` 問題數隨時間變化

| 年份 | 問題數 | 年增減 |
|---|---:|---:|
| 2020 | 315 | — |
| 2021 | 2,545 | +708% |
| 2022 | 3,921 | +54.1% |
| 2023 | 3,800 | −3.1% |
| 2024 | 2,796 | −26.4% |
| 2025 | 738 | −73.6% |
| 2026（至 11/18） | 98 | — |

歷年加總 14,213，與標籤總數 14,272 相差 59（合理誤差，可能來自早年標記變動），可見數據自洽。

**專業解讀**：Compose 問答量在 2022 達峰後逐年下滑，2025–2026 陡降。這未必代表技術衰退，更可能是**知識成熟、討論遷移到 Discord／Slack／GitHub Discussions**。對本專案的意涵：Compose 的「怎麼寫」問題變少，但「最佳實踐／效能／無障礙」問題仍在；Glance 連獨立標籤都沒有，代表**桌面小工具是文件中最需要補充情境的區塊**。

---

## 4. Hacker News 量化分析

| 查詢 | nbHits | 最高分故事 | 分數 | 留言 |
|---|---:|---|---:|---:|
| `Yibokgua` | **0** | — | — | — |
| `Jetpack Compose` | 131 | Jetpack Compose Alpha is released (2020) | 76 | 27 |
| `I Ching app` | 40 | Show HN: I Ching simulator with accurate Yarrow Stalk probabilities | 63 | 62 |
| `Google Glance widget` | 1 | Show HN: Android Hacker News Widget and App | 50 | 41 |

另一篇值得注意的近期文章：「I removed AI from my I Ching app」（2026-01-18，4 分／14 留言）。

**專業解讀**：

- 專案在 HN 完全沒有能見度（0 篇）。
- 「I Ching」主題有真實但溫和的興趣（40 篇，頭部 63 分／62 留言）。
- HN 對「移除 AI、強調離線／隱私」的易經應用有討論意願 → 這正是本專案的天然定位（完全離線、無第三方函式庫、無 AI），非常適合作為 **Show HN** 的敘事主軸。
- Glance 討論極少（1 篇），佐證 SO 的發現：小工具是文件與行銷的空白區。

---

## 5. 套件與下載統計

| 套件 | 版本 | 來源 | 直接下載數 | 可用的生態代理指標 |
|---|---|---|---|---|
| Kotlin | 2.1.0 | Maven Central | **No data available** | SO `kotlin` 97,579 題；JetBrains/kotlin：53,590★／6,466 forks／1,491 subscribers／539 open issues |
| Jetpack Compose（Material3 BOM） | 2025.02.00 | Google Maven | **No data available** | SO `android-jetpack-compose` 14,272 題 + 上表年度曲線；androidx/androidx：6,139★／1,413 forks／136 subscribers／63 open issues |
| Glance | 1.1.1 | Google Maven | **No data available** | SO `android-glance` 無標籤資料；HN 僅 1 篇 |
| AGP | 8.2.2 | Google Maven | **No data available** | — |
| Gradle | 8.13 | Gradle distribution | **No data available** | — |
| DataStore／Lifecycle／ViewModel | — | Google Maven | **No data available** | — |
| JUnit 4 | — | Maven Central | **No data available** | — |

**說明**：Maven Central 與 Google Maven **不公開單一 artifact 的下載次數**（不同於 npm 有 npm downloads API），因此精確下載數與「隨時間的下載曲線」在本報告中標記為 `No data available`。上表以 GitHub 倉庫指標與 StackOverflow 問答時間序列作為「生態關注度隨時間變化」的替代指標。

**專業提醒（可重現性風險）**：研究時 `gradle.properties` 依賴 `android.aapt2FromMavenOverride=/usr/bin/aapt2`，且 `local.properties` 的 SDK 路徑為本機路徑，對外部貢獻者是**建置阻礙**——新人 clone 後無法直接 `./gradlew build`。**後續已修正**：aapt2 覆寫已移除（見附錄 P0 完成狀態），SDK 路徑設定方式則寫入 `CONTRIBUTING.md`。

---

## 6. 產業競爭對手比較

GitHub 搜尋 `i ching android in:name,description` 共 **20** 個倉庫，頭部比較如下：

| 專案 | Stars | Forks | Open issues | 授權 | 建立 | 最後 push | 定位 |
|---|---:|---:|---:|---|---|---|---|
| **Yibokgua** | 0 | 0 | 0 | MIT | 2026-09 | 2026-11 | 5 種起卦 + 體用 + 九宮 + 閱讀器 + Glance 小工具 |
| IBK（同作者） | 0 | 0 | 0 | 無 | 2026-10 | — | 易卜卦（舊／重複倉庫） |
| moka992/zhifou-fortune | **53** | 0 | 0 | 無 | 2026-06 | 2026-10 | 離線優先、易經+塔羅+農曆、觸覺 3D、BYOK AI |
| aebrer/uncomplicatedIChing | 9 | 1 | 2 | GPL-3.0 | 2017-03 | 2023-11 | 簡單易經 App |

### 優勢（Benefits）

1. **領域完整性領先**：同時具備梅花易數、時間／類象／字占、九宮六壬、64 卦庫，功能廣度明顯高於 9★ 的對手。
2. **工程紀律良好**：單向模組邊界、演算法與資料分離（函式注入 `findHexagramByBinary`）、無第三方函式庫的檔案解析、Unihan 筆畫資料內建、83 個測試——以個人專案而言水準偏高。
3. **設計一致性**：本次重設計已統一 motion／字型／卡片 token，並處理符號字型缺字陷阱。
4. **授權明確**：MIT 比 zhifou-fortune（無授權）與 IBK（無授權）更易於再利用。

### 劣勢（Shortcomings）

1. **Zero 社群訊號**：0 star／0 fork／0 issue，且落後頭部對手 53★；缺乏發行版（對手 zhifou-fortune 有 releases 頁面作為 homepage）。
2. **無 CI、無分支保護、release 未簽章**（`release-keystore.jks` 未被 `signingConfig` 引用）→ 企業級信任度不足。
3. **建置環境耦合**：aapt2 override + 本機 SDK 路徑，降低新人貢獻意願。
4. **功能差異化未被訊息化**：離線、無 AI、無網路權限是賣點（呼應 HN 的「I removed AI」討論），但 README 把它寫成技術細節而非價值主張。
5. **歷史紀錄僅記憶體**：論述「記錄／回溯」情境時是明顯功能缺口（AGENTS.md 已標註 README 曾誇大此點）。
6. **重複倉庫**：IBK 與 Yibokgua 並存且無授權，會分散搜尋能見度與授權清晰度。

---

## 7. 建議補充的場景／情境（Scenarios & Contexts）

| 優先 | 場景 | 理由（資料依據） |
|---|---|---|
| P0 | **首次使用導覽**：用「脈動提示／微互動」引導 5 種起卦方式（TechCrunch 的 UX 文章特別強調 pulsing circles 與 microinteractions 降低 onboarding 摩擦） | HN／SO 顯示 Compose 圖文教程需求仍高；目前 app 無 onboarding 說明 |
| P0 | **離線 + 隱私 + 無 AI 的定位情境**：清楚寫出「不需網路、不上架 AI、資料只留裝置」 | HN「I removed AI from my I Ching app」有討論；zhifou-fortune 主打 BYOK AI，形成差異化 |
| P0 | **貢獻者入門情境**：CONTRIBUTING.md、issue／PR template、CHANGELOG、v0.x release tag、CI（compile + 83 tests） | 0 issue／0 PR／0 release；bus factor = 1；無 CI |
| P1 | **可解釋性情境**：把小工具的 S1／S2 mod 8／mod 6 計算過程擴展到所有起卦法，提供「為什麼是這卦」的步驟視圖 | 目前僅小工具顯示取數過程；提升信任與學習價值 |
| P1 | **桌面小工具使用情境**：「每 2 小時自動變卦」「每 30 分鐘九宮更新」「點擊直達詳情」的圖文範例 | SO `android-glance` 無資料、HN 僅 1 篇 → 文件空白即機會 |
| P1 | **無障礙與大字級情境**：說明字級設定、`YiType` 可縮放、符號字型、416dp 以上寬螢幕適配 | 既有 setting，但沒有使用情境文件 |
| P1 | **書籍閱讀器情境**：滙入自己的典籍（PDF／DOCX／MD／TXT）、離線閱讀、進度記錄 | 閱讀器是獨特功能，README 僅列規格未列使用故事 |
| P2 | **歷史／持久化情境**：要嘛實作持久化，要嘛在 UI 明確「工作階段結束即清空」 | 與 AGENTS.md 標註的文檔漂移一致 |
| P2 | **國際化情境**：英／簡體 README 與技術部落格文章 | 目前全 zh-TW，限制全球貢獻者 |
| P2 | **測試擴充情境**：把純邏輯測試從 `:app` 下沉到 `:engine`／`:data` | AGENTS.md 指出這三個模組目前無 test sources |

---

## 8. 技術部落格／網站檢視

| 來源 | 查詢 | 結果 |
|---|---|---|
| TechCrunch.com | `Yibokgua`／`alext78r39-ux`／`易卜卦` | 僅回傳無關文章（90 年代遊戲 UX、eBay 禁售靈性商品、iOS 7 redesign 等），**無本專案或同類 Android 易經 app 的報導** → **No data available** |
| Crunchbase.com | 同上 | 僅回傳同名「Alex Xu」人物頁、占星微產業分類表等，**無公司／融資／產品紀錄** → **No data available** |
| Hacker News | `Yibokgua` | 0 篇（見第 4 節） |

結論：本專案**沒有任何科技媒體或創投資料庫覆蓋**，這對個人開源 App 屬正常，但也意味著 DevRel 的可操作槓桿在於**自己去發布**（Show HN、開發者部落格、release notes），而非期待第三方報導。

---

## 9. 結論與優先行動

1. **先補社群基礎設施（P0）**：issue template、CONTRIBUTING、CHANGELOG、第一個 release tag + CI（compile + 83 tests）。
2. **把「離線、無 AI、無第三方庫」打成品牌訊息**，並用 Show HN 測試水溫（參考 I Ching 主題 63 分／62 留言的頭部故事）。
3. **補 Glance 小工具與 onboarding 的使用情境文件**——這兩個區塊在 SO／HN 都是資料空白，最容易建立內容差異化。
4. **修正文檔漂移與建置 bootstrap**（aapt2／SDK 路徑），降低貢獻門檻。
5. **處理重複倉庫 IBK**（補授權或封存），避免授權與能見度稀釋。

---

## 附錄：P0 完成狀態（2026-11-18）

| P0 項目 | 狀態 | 交付物 |
|---|---|---|
| 首次使用導覽 | 已完成 | `ui/components/OnboardingGuide.kt`；五步驟、脈動微互動、DataStore 持久化 `onboardingSeen`；已接至 `MainActivity` → `DivinationInputScreen` |
| 離線／隱私／無 AI 定位 | 已完成 | README「設計定位」章節；App 內「設定 → 說明 → 離線與隱私」對話框；「關於易卜」補充說明 |
| 貢獻者入門 | 已完成 | `CONTRIBUTING.md`、`CHANGELOG.md`、issue／PR 模板、GitHub Actions CI（編譯 + 83 測試 + APK 產物）、已推送 tag `v1.0.0` 與 GitHub Release |

驗證結果：`compileDebugKotlin + testDebugUnitTest + assembleDebug` 全部通過（14 套件／83 測試／0 失敗）。`gradle.properties` 中寫死的 `/usr/bin/aapt2` 覆寫已移除，由 AGP 使用 Maven 提供的平台對應 aapt2；CI 亦不再需要 `sed` 清理步驟，新人 clone 後可直接同步建置。
