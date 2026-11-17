# 變更紀錄

本專案遵循 [Keep a Changelog](https://keepachangelog.com/zh-TW/1.1.0/) 格式，版本號語意化。

## [Unreleased]

### Added
- 首次使用導覽：首次進入起卦頁時以五個步驟介紹手動、快速、日期、類象、字占五種方式，含脈動微互動；看過後寫入 DataStore，可至「設定 → 使用說明」再次查看。
- 「設定 → 說明 → 離線與隱私」說明頁，明確標示不需網路權限、不傳送資料、無 AI、無第三方解析函式庫。
- 貢獻者文件：`CONTRIBUTING.md`、issue／PR 模板、CI workflow（編譯 + 83 個單元測試 + 產出 APK）。
- Developer Relations 研究報告 `DEVEL_RELATIONS_REPORT.md`。

### Changed
- README 新增「設計定位：離線、隱私、無 AI」章節，並連結貢獻指南與變更紀錄。
- 「關於易卜」對話框補充離線與無 AI 說明。

## [1.0.0] - 2026-11-18

### Added
- 墨紙系介面重塑：統一 `YiMotion` 動效 token、`YiType` 可縮放字級與 `ZenCard` 卡片樣式。
- 5 種起卦方式、體用生克解析、64 卦庫、九宮六壬、書籍閱讀器（PDF／DOC／DOCX／MD／TXT）、2 款 Glance 桌面小工具、詳情卡片 PNG 導出。

### Changed
- 合併 `PaperCard` 視覺至 `ZenCard`；移除 `ZenMotion`／`LocalMotion`，改由 `YiMotion` 單一來源提供動效。
- 還原錯誤色、太極 token 色與小工具深色配色。

### Fixed
- 本機 Windows 建置所需的 aapt2 路徑覆寫；`git` 檔案模式設定還原。

[Unreleased]: https://github.com/alext78r39-ux/Yibokgua/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/alext78r39-ux/Yibokgua/releases/tag/v1.0.0
