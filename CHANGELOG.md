# 變更日誌 (Changelog)

本專案的所有重要變更都會記錄在此檔案中。

格式基於 [Keep a Changelog](https://keepachangelog.com/zh-TW/1.0.0/)，
本專案遵守 [語義化版本](https://semver.org/lang/zh-TW/)。

---

## [4.0.0] - 2025-03-14

### 新增 (Added)

#### 核心功能
- ✅ Memory Dump 可視化 - RecyclerView 表格顯示記憶體傾印數據
- ✅ DumpAdapter.java - RecyclerView 表格適配器，管理記憶體資料項目
- ✅ DumpItem.java - 扇區/區塊/資料/描述資料模型
- ✅ 專業版品牌 - "HeliRFID 專業版" 標示和品牌識別
- ✅ activity_memory_dump.xml - Memory Dump 专用 Activity 佈局
- ✅ dump_item.xml - RecyclerView 單一項目卡片佈局

#### 技術實作
- ✅ RecyclerView 依賴 - 添加 androidx.recyclerview:recyclerview:1.3.2
- ✅ MemoryDumpActivity - 專業的記憶體傾印檢視介面
- ✅ 區塊化資訊顯示 - 扇區、區塊、資料、描述四欄位

#### 使用者介面
- ✅ 專業版標題 - activity_main.xml 更新為 "HeliRFID 專業版"
- ✅ 表格化資訊呈現 - 清晰的表格顯示記憶體內容
- ✅ 精美卡片設計 - dump_item 優美卡片佈局
- ✅ 專業版品牌 - AndroidManifest.xml 應用程式標籤更新

### 變更 (Changed)

#### 套件重構
- 套件名稱重構：com.helirfid.ultimate → com.helirfid
- 所有 Java 檔案套件名更新至 com.helirfid
- 移除 Ultimate 版本專用功能模組

#### 程式碼簡化
- 移除 CloneAnalyzer.java - 複製風險分析功能
- 移除 DumpReader.java - 舊版記憶體傾印功能
- 移除 KeyTester.java - 金鑰測試功能
- 專注於核心 NFC 讀取功能

#### 版本資訊
- 版本號更新：versionCode 7，versionName "4.0.0"
- AndroidManifest.xml 更新應用程式標籤為 "HeliRFID 專業版"
- build.gradle 更新版本和新增 RecyclerView 依賴

#### 使用者介面
- MainActivity 標題更新為專業版
- 啟動畫面更新為專業版品牌識別
- 統一品牌配色和設計語言

### 移除 (Removed)

- ❌ CloneAnalyzer.java - 複製風險分析模組
- ❌ DumpReader.java - 舊版記憶體傾印功能（替換為 DumpAdapter/DumpItem）
- ❌ KeyTester.java - 金鑰測試功能
- ❌ com.helirfid.ultimate 套件 - Ultimate 版本套件結構

### 優化 (Optimized)

- 簡化功能列表，專注於核心 NFC 讀取體驗
- 優化 Memory Dump 介面設計，使用 RecyclerView 提升效能
- 專業版 UI/UX 統一設計
- 清晰的表格化資訊呈現

### 依賴庫
- compileSdk: 34 (Android 14)
- targetSdk: 34 (Android 14)
- 增加：androidx.recyclerview:recyclerview:1.3.2

---

## [3.0.0] - 2025-03-14

### 新增 (Added)

#### 核心功能
- ✅ Memory Dump 功能 - 讀取 Mifare Classic 卡片的記憶體區塊
- ✅ 增強型金鑰測試 - 測試 5 個常見金鑰集（工廠預設、Madroid、NXP、Uncommon、Public）
- ✅ 金鑰測試結果顯示 - 即時顯示測試成功的金鑰資訊
- ✅ 匯出 CSV 按鈕功能 - 將歷史記錄匯出為 CSV 檔案

#### 程式碼模組
- ✅ DumpReader.java - Mifare Classic 記憶體傾印功能
- ✅ KeyTester.java (增強版) - 5 個金鑰集測試功能

### 變更 (Changed)

#### 技術改進
- 將 MainActivity 和 SplashActivity 從 AppCompatActivity 改為 Activity
- 版本號更新：versionCode 3，versionName "3.0"
- 應用程式標籤更新為 "HeliRFID Ultimate v3"
- 整合 DumpReader 和 KeyTester 到 MainActivity
- 新增 Memory Dump 按鈕功能
- 新增 txtKey 視圖用於顯示測試結果
- 更新啟動畫面為 v3.0 版本品牌識別

#### 使用者介面
- 新增金鑰測試結果顯示欄位
- 新增 Memory Dump 和匯出按鈕佈局
- 更新 splash.xml 顯示 "HeliRFID Ultimate v3.0"
- 按鈕群組優化（Dump、Clear、Export）

#### 金鑰測試功能
- 測試工廠預設金鑰：FF FF FF FF FF FF
- 測試 Madroid 金鑰：A0 A1 A2 A3 A4 A5
- 測試 NXP 金鑰：D3 F7 D3 F7 D3 F7
- 測試 Uncommon 金鑰：4D 3A 99 CB 34 0B
- 測試 Public 金鑰：1A 98 2C 7E 45 9A

### 修復 (Fixed)
- 修正 MainActivity 中 TextView ID 對應問題
- 修正呼叫 Converter 和 CardAnalyzer 的方法名稱

### 依賴庫
- targetSdk 更新至 34 (Android 14)
- compileSdk 更新至 34

---

## [2.0.0] - 2025-03-14

### 新增 (Added)

#### 核心功能
- ✅ NFC 卡片自動讀取
- ✅ UID 雙重轉換格式（HEX / 10碼 / 8碼）
- ✅ Wiegand 通訊協定轉換（Wiegand26）
- ✅ 卡片類型識別（MIFARE Classic / Ultralight / ISO 14443-4）
- ✅ 複製風險分析（Clone Analyzer）
- ✅ 記憶體傾印功能（Mifare Classic 可讀取區塊）
- ✅ CSV 歷史記錄匯出功能
- ✅ 50+ 種門禁設備演算法框架

#### 使用者介面
- ✅ Material3 設計風格
- ✅ MaterialCardView 卡片佈局
- ✅ Scrollable 介面支援長內容
- ✅ 深藍色主題配色 (#1A237E)
- ✅ 升級版 HR Logo（Vector Drawable）
- ✅ 深色模式支援（自動）

#### 技術實作
- ✅ 完整包名重構：com.helirfid.ultimate
- ✅ Android X 相容性完整支援
- ✅ Min SDK 23 (Android 6.0)
- ✅ Target SDK 34 (Android 14)
- ✅ Theme.Material3.DayNight.NoActionBar

#### 程式碼模組
- ✅ NFCReader.java - NFC UID 讀取
- ✅ Wiegand.java - Wiegand26/34 通訊協定轉換
- ✅ CloneAnalyzer.java - 複製風險偵測
- ✅ CardAnalyzer.java - 卡片類型識別
- ✅ CsvExporter.java - CSV 匯出功能

#### 專案結構
- ✅ 完整的 README 說明文檔
- ✅ 貢獻指南 (CONTRIBUTING.md)
- ✅ 變更日誌 (CHANGELOG.md)
- ✅ 開發指南 (DEVELOPMENT.md)
- ✅ MIT 授權檔案

### 變更 (Changed)

#### 套件重構
- 從 com.helirfid.app 重構為 com.helirfid.ultimate
- 所有 Java 檔案套件名更新

#### 依賴更新
- Material Design：更新至 1.11.0
- compileSdk：更新至 34
- targetSdk：更新至 34

### 技術改進
- 改進 UI 回應速度
- 優化 NFC 讀取穩定性
- 改進錯誤處理

---

## [1.1.0] - 2025-03-14

### 新增 (Added)

#### 功能增強
- ✅ 卡號自動補滿 10 碼（不足時在前面補零）
- ✅ 清除歷史紀錄按鈕
- ✅ 清除歷史記錄後 Toast 提示
- ✅ HistoryManager 清除方法

#### 使用者介面改進
- ✅ Material Design 風格優化
- ✅ 美化主畫面佈局
- ✅ 藍色標題文字 (#1565C0)
- ✅ 灰色副標題文字 (#666)
- ✅ 淺灰色背景 (#F5F5F5)
- ✅ 結果顯示區白色背景卡片
- ✅ 結果卡片陰影效果 (elevation="3dp")
- ✅ 清除歷史紀錄按鈕紅色配色 (#E53935)
- ✅ 按鈕文字更改：「轉換」→「轉換卡號」
- ✅ 更好的間距和排版

### 變更 (Changed)

#### 轉換邏輯
- 卡號現在始終為固定 10 碼（之前可能少於 10 碼）
- 使用前面補零的方式確保固定長度

#### 使用者介面
- 按鈕寬度從 `wrap_content` 改為 `match_parent`
- 增加按鈕間距和內邊距
- 歷史紀錄標題改為「歷史紀錄」

### 技術改進
- 改進 UID 轉換算法（補零邏輯）
- HistoryManager 新增 `clear()` 方法
- MainActivity 新增清除按鈕處理邏輯

### 修復 (Fixed)
- 無

### 文件 (Documentation)
- 無

---

## [1.0.0] - 2025-03-14

### 新增 (Added)

初次正式發布 v1.0.0

#### 核心功能
- ✅ NFC 卡片自動讀取
- ✅ UID 原始值顯示（16 進制格式）
- ✅ UID 轉門禁卡號功能（後 10 碼）
- ✅ 手動輸入 UID 轉換功能
- ✅ 卡號歷史記錄管理（最多 100 筆）

#### 使用者介面
- ✅ 啟動動畫畫面（2 秒延遲）
- ✅ 主畫面佈局 (LinearLayout)
- ✅ 品牌標示顯示："HeliRFID" 和 "禾秝軟體開發團隊"
- ✅ 掃描結果顯示區域
- ✅ 手動輸入欄位
- ✅ 卡號歷史列表

#### 圖示與設計
- ✅ HR 品牌圖示（Vector Drawable）
- ✅ 藍色背景設計 (#1565C0)
- ✅ 適配圖示 (Adaptive Icon) 支援
- ✅ 啟動圖示

#### 技術實作
- ✅ Android NFC API 整合
- ✅ SharedPreferences 歷史記錄儲存
- ✅ BigInteger 大數運算支援
- ✅ Android X 相容性
- ✅ Min SDK 24 (Android 7.0)
- ✅ Target SDK 33 (Android 13)

#### 專案結構
- ✅ Gradle 構建系統
- ✅ 模組化程式碼架構
- ✅ 完整的 README 說明文檔
- ✅ .gitignore 配置
- ✅ MIT 授權檔案
- ✅ 貢獻指南 (CONTRIBUTING.md)
- ✅ 變更日誌 (CHANGELOG.md)

#### 依賴庫
- androidx.appcompat:appcompat:1.6.1

### 修復 (Fixed)

- 修正 Android 12+ 適配問題（添加 `android:exported` 屬性）

### 文件 (Documentation)

- ✅ README.md - 完整的專案說明
- ✅ 專案結構說明
- ✅ 使用指南
- ✅ 安裝說明
- ✅ 貢獻指南
- ✅ 授權檔案
- ✅ 變更日誌

---

## [未發布]

### 計劃中 (Planned)

#### 功能增強
- [ ] 支援更多 NFC 卡片類型（MIFARE Classic 等）
- [ ] 卡號複製到剪貼板
- [ ] 歷史記錄匯出/匯入功能
- [ ] 卡號搜尋功能
- [ ] 卡片分類/標籤管理
- [ ] 卡號備註功能

#### 改進
- [ ] 深色模式支援
- [ ] 多語言支援（繁體中文、簡體中文、英文）
- [ ] 掃描速度優化
- [ ] 錯誤處理改善
- [ ] 使用者介面優化（Material Design 3）
- [ ] 動畫效果優化

#### 技術改進
- [ ] 單元測試覆蓋
- [ ] 代碼品質檢查（SonarQube）
- [ ] CI/CD 自動化
- [ ] Release APK 簽名
- [ ] Google Play 商店上架

#### 文件改進
- [ ] API 文檔
- [ ] 開發者指南 (DEVELOPMENT.md)
- [ ] 故障排除指南 (TROUBLESHOOTING.md)
- [ ] 更多使用範例

---

## 版本說明

### 版本編號格式

遵循語義化版本：`MAJOR.MINOR.PATCH`

- **MAJOR**：不相容的 API 變更
- **MINOR**：向下相容的功能性新增
- **PATCH**：向下相容的問題修正

### 變更類型

- **新增 (Added)**: 新功能
- **變更 (Changed)**: 對現有功能的變更
- **棄用 (Deprecated)**: 即將移除的功能
- **移除 (Removed)**: 已移除的功能
- **修復 (Fixed)**: Bug 修復
- **安全性 (Security)**: 安全性變更

---

## 貢獻者

### v1.0.0
- js0935 - 初始開發

---

## 參考

- [HeliRFID GitHub](https://github.com/js0935/HeliRFID)
- [問題追踪](https://github.com/js0935/HeliRFID/issues)
- [貢獻指南](CONTRIBUTING.md)
