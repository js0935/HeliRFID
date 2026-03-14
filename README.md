# HeliRFID - NFC 門禁卡讀取器

<div align="center">

**禾秝軟體開發團隊**

一款簡單易用的 Android NFC 門禁卡讀取應用程式

[![Version](https://img.shields.io/badge/version-v4.0.1-blue.svg)](https://github.com/js0935/HeliRFID/releases/latest)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)
[![Android](https://img.shields.io/badge/android-6.0%2B-green.svg)](https://developer.android.com/)

</div>

---

## 🌟 什麼是 HeliRFID？

HeliRFID 是一款專為 Android 手機設計的 NFC 門禁卡讀取工具，可以快速讀取各類 NFC 卡片，並將卡片 ID 轉換為門禁系統可用的卡號格式。

**主要特色：**
- 📱 自動讀取 NFC 卡片
- 🔢 一鍵轉換多種卡號格式（10 碼 / 8 碼）
- 🔐 支援 Wiegand 門禁協定
- 🔑 金鑰測試功能（Mifare Classic）
- 🛡️ 複製風險分析
- 📊 記憶體檢視功能
- 📝 歷史記錄管理
- 📤 CSV 匯出功能

---

## 📥 下載與安裝

### 系統需求

| 項目 | 需求 |
|------|------|
| 作業系統 | Android 6.0 (API 23) 或更高版本 |
| NFC | 必須支援 NFC 功能 |
| 儲存空間 | 約 10 MB |
| 權限 | NFC、儲存空間 |

### 下載 APK

**最新版本：v4.0.1 Complete Edition** (2025-03-14)

[📥 下載 HeliRFIDProfessional-v4.0.1-complete.apk](https://github.com/js0935/HeliRFID/raw/refs/tags/v4.0.1-complete/releases/HeliRFIDProfessional-v4.0.1-complete.apk) (5.3 MB)

**✨ v4.0.1 Complete Edition 新增功能：**
- 🔑 金鑰測試（Key Tester）- 測試 Mifare Classic 5 種常見金鑰
- 🛡️ 複製風險分析（Clone Analyzer）- 偵測並評估複製卡風險
- 📝 手動 UID 輸入功能
- 📦 完整歷史記錄管理（100 筆限制 + Toast 提示）

### 安裝步驟

1. 下載 APK 檔案
2. 在手機上開啟 APK
3. 允許「來源不明應用程式」安裝
4. 完成安裝並開啟

**⚠️ 注意：** 此應用程式需要 NFC 功能，請確保您的手機支援 NFC。

---

## 🚀 快速開始

### 第一次使用

1. **開啟 NFC 功能**
   - 前往「設定」→「NFC」
   - 確保 NFC 已開啟

2. **開啟應用程式**
   - 啟動 HeliRFID 應用程式
   - 等待啟動畫面完成

3. **掃描卡片**
   - 將 NFC 卡片靠近手機背面
   - 保持距離 1-3 公分
   - 等待讀取完成

### 讀取結果說明

應用程式會顯示以下資訊：

| 項目 | 說明 | 格式範例 |
|------|------|----------|
| UID 原始值 | NFC 卡片的唯一識別碼 | `04:A2:B3:C4:D5:E6:78` |
| 10 碼卡號 | 門禁系統常用的 10 位數字卡號 | `2029985476` |
| 8 碼卡號 | 較短的 8 位數字卡號 | `985476` |
| Wiegand26 | 門禁系統通訊協定格式 | `0000-78E6-D5C4-0000` |
| 卡片類型 | NFC 卡片類型識別 | MIFARE Classic / Ultralight |

---

## 🎯 主要功能

### 1. NFC 卡片讀取
- 自動讀取各類 NFC 卡片
- 支援 MIFARE Classic、Ultralight、ISO 14443 等格式
- 即時顯示讀取結果

### 2. 卡號轉換
UID 自動轉換為多種格式：
- **10 碼格式**：常用於門禁系統
- **8 碼格式**：簡短卡號
- **Wiegand26/34**：門禁系統通訊協定
- **HEX 格式**：16 進制 UID

### 3. 歷史記錄
- 自動儲存最近 100 筆掃描記錄
- 採最新記錄排在最前
- 支援一鍵清除所有記錄

### 4. CSV 匯出
- 將歷史記錄匯出為 CSV 檔案
- 儲存於 Downloads 目錄
- 可用於試算表軟體開啟

### 5. 金鑰測試 (Key Tester)
- 測試 Mifare Classic 卡片的常見金鑰
- 支援 5 種金鑰集：
  - 工廠預設：`FF FF FF FF FF FF`
  - Madroid：`A0 A1 A2 A3 A4 A5`
  - NXP：`D3 F7 D3 F7 D3 F7`
  - Uncommon：`4D 3A 99 CB 34 0B`
  - Public：`1A 98 2C 7E 45 9A`
- 顯示測試成功的金鑰資訊

### 6. 複製風險分析 (Clone Analyzer)
- 偵測 NFC 卡片的複製風險
- 分析 UID 模式以識別潛在的複製卡
- 提供風險等級和防護建議
- 適用於門禁系統的安全檢測

### 7. Memory Dump

---

## ❓ 常見問題

### 一般問題

**Q: 應用程式免費嗎？**
A: 是的，HeliRFID 完全免費且開源。

**Q: 需要網路連線嗎？**
A: 不需要，所有功能都在離線狀態下運作。

**Q: 支援哪些卡片？**
A: 支援大部分 13.56 MHz NFC 卡片：
- MIFARE Classic (1k, 4k)
- MIFARE Ultralight
- ISO 14443 Type A/B
- FeliCa 卡片

**Q: 在模擬器上能運行嗎？**
A: 不可以，模擬器不支援 NFC 硬體，需要實體手機測試。

### 使用問題

**Q: NFC 不讀取怎麼辦？**
A: 請檢查：
1. NFC 功能是否已開啟（設定 → NFC）
2. 卡片與手機距離是否在 1-3 公分內
3. 是否授予 NFC 權限

**Q: CSV 匯出檔案在哪裡？**
A: 儲存在手機的 Downloads 目錄中：
- 路徑：`Android/storage/emulated/0/Download/`
- 檔名：`HeliRFID_History.csv`

**Q: 如何清除歷史記錄？**
A: 點擊「清除紀錄」按鈕，確認後即可清除所有記錄。

**Q: 轉換結果不正確？**
A: 請確認：
1. UID 格式是否正確
2. 使用正確類型的 NFC 卡片
3. 某些特殊卡片可能不支援轉換

### 隱私與權限

**Q: 應用程式會上傳資料嗎？**
A: 不會，所有資料僅儲存在您的手機上，不會上傳到任何伺服器。

**Q: 需要哪些權限？**
A:
- **NFC 權限**：讀取 NFC 卡片
- **儲存空間權限**：匯出 CSV 檔案

---

## 📖 詳細說明

### UID 轉換流程

HeliRFID 使用標準演算法將 NFC 卡片的 UID 轉換為門禁卡號：

```
原始 UID: 04:A2:B3:C4:D5:E6:78
    ↓ 移除冒號
04A2B3C4D5E678
    ↓ 取後 8 碼
C4D5E678
    ↓ 反轉位元組
78E6D5C4
    ↓ 轉 10 進制
2029985476
    ↓ 取後 10 碼
2029985476 (最終卡號)
```

### 從原始碼編譯

如果您想自行編譯應用程式：

```bash
# 複製專案
git clone https://github.com/js0935/HeliRFID.git
cd heli_rfid_nfc

# 編譯 Debug 版本
./gradle-8.0/bin/gradle assembleDebug

# APK 輸出路徑
app/build/outputs/apk/debug/app-debug.apk
```

詳細開發資訊請參閱 [DEVELOPMENT.md](DEVELOPMENT.md)

---

## 🛠 技術資訊

### 開發技術

| 技術 | 版本 |
|------|------|
| 開發語言 | Java 8 |
| 最低 SDK | Android 6.0 (API 23) |
| 目標 SDK | Android 14 (API 34) |
| Gradle | 8.0 |
| Material Design | 1.11.0 |

### 專案結構

```
heli_rfid_nfc/
├── app/
│   └── src/main/java/com/helirfid/
│       ├── MainActivity.java          # 主畫面
│       ├── SplashActivity.java        # 啟動畫面
│       ├── MemoryDumpActivity.java    # 記憶體檢視
│       ├── Converter.java             # 轉換邏輯
│       ├── NFCReader.java             # NFC 讀取
│       ├── Wiegand.java               # Wiegand 協定
│       ├── CardAnalyzer.java          # 卡片分析
│       ├── CloneAnalyzer.java         # 複製風險分析
│       ├── KeyTester.java             # 金鑰測試
│       ├── HistoryManager.java        # 歷史管理
│       ├── CsvExporter.java           # CSV 匯出
│       ├── DumpAdapter.java           # RecyclerView 適配器
│       └── DumpItem.java              # 記憶體資料模型
├── README.md                          # 本檔案
├── CHANGELOG.md                       # 版本更新日誌
├── DEVELOPMENT.md                     # 開發者指南
└── LICENSE                            # MIT 授權
```

---

## 📋 版本資訊

### v4.0.1 (2025-03-14) - Complete Edition (完整版)

**完整功能整合：**
- ✅ 整合歷代版本所有功能（v1.0.0 ~ v4.0.0）
- ✅ 還原金鑰測試功能（KeyTester - v3.0.0）
- ✅ 還原複製風險分析（CloneAnalyzer - v2.0.0）
- ✅ 還原手動 UID 輸入功能（v1.0.0）

**修復：**
- ✅ 修復版本顯示（從 v3.0 更新為 v4.0.1）
- ✅ 修復啟動畫面標題（更新為「HeliRFID 專業版」）
- ✅ 修復 CSV 匯出（支援 Android 10+）

**歷史功能列表：**
- v1.0.0: 手動輸入、10 位數卡號、歷史記錄
- v1.1.0: 100 筆限制、清除記錄 Toast、自動補零
- v2.0.0: Wiegand26/34、CloneAnalyzer、CSV 匯出、多種卡片格式
- v3.0.0: KeyTester 五種常見金鑰測試
- v4.0.0: Memory Dump 可視化、Android 14 支援

### v4.0.0 (2025-03-14) - 專業版發布

**新增：**
- ✅ Memory Dump 可視化（RecyclerView 表格）
- ✅ 專業版品牌識別
- ✅ Android 14 完整支援

**變更：**
- 套件名稱重構：`com.helirfid`
-簡化功能，專注核心 NFC 讀取

查看完整變更：[CHANGELOG.md](CHANGELOG.md)

---

## 🤝 貢獻與支援

### 貢獻

我們熱烈歡迎您的貢獻！

1. Fork 專案
2. 創建功能分支：`git checkout -b feature/AmazingFeature`
3. 提交變更：`git commit -m 'Add some AmazingFeature'`
4. 推送分支：`git push origin feature/AmazingFeature`
5. 開啟 Pull Request

詳細指南請參閱 [CONTRIBUTING.md](CONTRIBUTING.md)

### 回報問題

如遇到 Bug 或有功能建議：
- 前往 [Issues 頁面](https://github.com/js0935/HeliRFID/issues)
- 搜索是否有相似的問題
- 開啟新 Issue 並詳細描述

---

## 📞 聯絡方式

- **GitHub**: [js0935/HeliRFID](https://github.com/js0935/HeliRFID)
- **問題回報**: [GitHub Issues](https://github.com/js0935/HeliRFID/issues)

---

## 📄 授權

本專案採用 [MIT License](LICENSE) 授權。

```
Copyright (c) 2025 禾秝軟體開發團隊 (HeliRFID Development Team)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction...
```

---

## 🙏 致謝

感謝以下專案和資源：

- [Android Developers](https://developer.android.com/)
- [Material Design](https://material.io/design)
- [AndroidX](https://developer.android.com/jetpack/androidx)
- 所有貢獻者和使用者

---

<div align="center">

**Made with ❤️ by [禾秝軟體開發團隊](https://github.com/js0935)**

[⬆ 回到頂部](#helifid---nfc-門禁卡讀取器)

⭐ 如果喜歡這個專案，請給我們一顆星星！

</div>
