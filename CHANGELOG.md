# 變更日誌 (Changelog)

查看所有重要的專案變更。

本專案採用 [語義化版本](https://semver.org/lang/zh-TW/)。

**格式說明：**
- `✅ 新增` - 新功能
- `🔧 變更` - 對現有功能的變更
- `🐛 修復` - Bug 修復
- `❌ 移除` - 已移除的功能

---

## 📅 版本歷史

### [4.0.1] - 2025-03-14 - Complete Edition (完整版)

#### ✅ 新增 (Added)

**歷史功能整合**
- ✅ 還原並整合 CloneAnalyzer 複製風險分析功能（原 v2.0.0 功能）
  - CloneAnalyzer.java - 複製卡風險偵測
  - 偵測 UID 模式並評估複製風險等級
  - 回報風險類型和防護建議
- ✅ 還原並整合 KeyTester 金鑰測試功能（原 v3.0.0 功能）
  - KeyTester.java - Mifare Classic 金鑰測試
  - 測試 5 種常見金鑰：工廠預設、Madroid、NXP、Uncommon、Public
  - 即時顯示測試成功的金鑰資訊和區塊存取權限
- ✅ 還原手動 UID 輸入功能（原 v1.0.0 功能）
  - EditText 手動輸入欄位和轉換按鈕
  - 支援多種 UID 格式輸入
- ✅ 還原清除歷史記錄 Toast 提示（原 v1.1.0 功能）
  - 清除記錄後顯示確認訊息
- ✅ 還原 100 筆歷史記錄限制邏輯（原 v1.0.0 功能）
  - HistoryManager 自動維護記錄數量上限

**使用者介面**
- ✅ 複製風險分析區塊
  - 顯示風險等級、風險類型、防護建議
  - "分析複製風險" 按鈕
- ✅ 金鑰測試區塊
  - 顯示測試成功的金鑰
  - "測試金鑰" 按鈕
  - 自動關聯當前掃描的標籤

#### 🐛 修復 (Fixed)

- 🔧 修復啟動畫面版本號顯示
  - 從 "v3.0" 更新為 "v4.0.1"
- 🔧 修復啟動畫面標題顯示
  - 從 "HeliRFID Ultimate" 更新為 "HeliRFID 專業版"
- 🔧 修復 CSV 匯出功能
  - 支援 Android 10+ (API 29+) 使用 MediaStore API
  - 保留舊的檔案寫入方法以支援 Android 9 以下
  - 改進資源管理，使用 try-with-resources 確保正確關閉資源
  - 新增異常處理和堆疊追蹤輸出

#### 🔧 技術改進

- MainActivity.java 完整功能整合
  - 新增 txtAnalysis、txtKeyTest 視圖
  - 新增 btnAnalyze、btnTestKeys 事件處理
  - 整合 CloneAnalyzer 和 KeyTester 到主流程
- activity_main.xml 佈局更新
  - 新增複製風險分析 CardView 區塊
  - 新增金鑰測試 CardView 區塊
  - 新增手動輸入 EditText 和轉換按鈕

#### 🔧 技術實作

- 實現 `exportWithMediaStore()` 方法
  - 使用 ContentResolver 和 MediaStore API
  - 支援 `MediaStore.Downloads.RELATIVE_PATH` 和 `DISPLAY_NAME` 設置
  - 支援 MIME 類型設置為 `text/csv`
- 實現 `exportLegacy()` 方法
  - 使用傳統的 File IO
  - 相容 Android 9 以下版本
- 根據 `Build.VERSION.SDK_INT` 自動選擇適當的匯出方法

#### 📦 版本特點

**v4.0.1 Complete Edition (完整版)** 整合了所有歷代版本功能：
- ✅ v1.0.0: 手動輸入、10 位數卡號、歷史記錄
- ✅ v1.1.0: 100 筆限制、清除記錄 Toast、自動補零
- ✅ v2.0.0: Wiegand26/34、CloneAnalyzer、CSV 匯出、多種卡片格式
- ✅ v3.0.0: KeyTester 五種常見金鑰測試
- ✅ v4.0.0: Memory Dump 可視化、Android 14 支援
- ✅ v4.0.1: 版本顯示修復、CSV 匯出 Android 10+ 支援

#### 📦 依賴

- Android 10+ (API 29): MediaStore API
- Android 9 以下 (API 28-): 傳統 External Storage API

---

### [4.0.0] - 2025-03-14 - 專業版發布

#### ✅ 新增 (Added)

**核心功能**
- ✅ Memory Dump 可視化
  - RecyclerView 表格顯示記憶體傾印數據
  - DumpAdapter.java - RecyclerView 表格適配器
  - DumpItem.java - 扇區/區塊/資料/描述資料模型
- ✅ 專業版品牌
  - "HeliRFID 專業版" 標示和品牌識別
  - activity_memory_dump.xml - Memory Dump 專用 Activity 佈局
  - dump_item.xml - RecyclerView 單一項目卡片佈局

**技術實作**
- ✅ RecyclerView 依賴
  - 添加 androidx.recyclerview:recyclerview:1.3.2
- ✅ MemoryDumpActivity
  - 專業的記憶體傾印檢視介面
- ✅ 區塊化資訊顯示
  - 扇區、區塊、資料、描述四欄位

**使用者介面**
- ✅ 專業版標題
  - activity_main.xml 更新為 "HeliRFID 專業版"
- ✅ 表格化資訊呈現
  - 清晰的表格顯示記憶體內容
- ✅ 精美卡片設計
  - dump_item 優美卡片佈局
- ✅ 專業版品牌
  - AndroidManifest.xml 應用程式標籤更新

#### 🔧 變更 (Changed)

**套件重構**
- 套件名稱重構：`com.helirfid.ultimate` → `com.helirfid`
- 所有 Java 檔案套件名更新至 com.helirfid
- 移除 Ultimate 版本專用功能模組

**程式碼簡化**
- 專注於核心 NFC 讀取功能體驗
- 移除不必要的分析功能

**版本資訊**
- 版本號更新：versionCode 7，versionName "4.0.0"
- AndroidManifest.xml 更新應用程式標籤為 "HeliRFID 專業版"
- build.gradle 更新版本和新增 RecyclerView 依賴

**使用者介面**
- MainActivity 標題更新為專業版
- 啟動畫面更新為專業版品牌識別
- 統一品牌配色和設計語言

#### ❌ 移除 (Removed)

- ❌ CloneAnalyzer.java - 複製風險分析模組
- ❌ DumpReader.java - 舊版記憶體傾印功能（替換為 DumpAdapter/DumpItem）
- ❌ KeyTester.java - 金鑰測試功能
- ❌ com.helirfid.ultimate 套件 - Ultimate 版本套件結構

#### ✨ 優化 (Optimized)

- 簡化功能列表，專注於核心 NFC 讀取體驗
- 優化 Memory Dump 介面設計，使用 RecyclerView 提升效能
- 專業版 UI/UX 統一設計
- 清晰的表格化資訊呈現

#### 📦 依賴

- compileSdk: 34 (Android 14)
- targetSdk: 34 (Android 14)
- 增加：androidx.recyclerview:recyclerview:1.3.2

---

### [3.0.0] - 2025-03-14 - Ultimate Edition

#### ✅ 新增 (Added)

**核心功能**
- ✅ Memory Dump 功能
  - 讀取 Mifare Classic 卡片的記憶體區塊
- ✅ 增強型金鑰測試
  - 測試 5 個常見金鑰集
  - 工廠預設、Madroid、NXP、Uncommon、Public
- ✅ 金鑰測試結果顯示
  - 即時顯示測試成功的金鑰資訊
- ✅ 匯出 CSV 按鈕功能
  - 將歷史記錄匯出為 CSV 檔案

**程式碼模組**
- ✅ DumpReader.java - Mifare Classic 記憶體傾印功能
- ✅ KeyTester.java (增強版) - 5 個金鑰集測試功能

#### 🔧 變更 (Changed)

**技術改進**
- MainActivity 和 SplashActivity 從 AppCompatActivity 改為 Activity
- 版本號更新：versionCode 3，versionName "3.0"
- 應用程式標籤更新為 "HeliRFID Ultimate v3"
- 整合 DumpReader 和 KeyTester 到 MainActivity
- 新增 Memory Dump 按鈕功能
- 新增 txtKey 視圖用於顯示測試結果
- 更新啟動畫面為 v3.0 版本品牌識別

**使用者介面**
- 新增金鑰測試結果顯示欄位
- 新增 Memory Dump 和匯出按鈕佈局
- 更新 splash.xml 顯示 "HeliRFID Ultimate v3.0"
- 按鈕群組優化（Dump、Clear、Export）

**金鑰測試功能**
- 測試工廠預設金鑰：`FF FF FF FF FF FF`
- 測試 Madroid 金鑰：`A0 A1 A2 A3 A4 A5`
- 測試 NXP 金鑰：`D3 F7 D3 F7 D3 F7`
- 測試 Uncommon 金鑰：`4D 3A 99 CB 34 0B`
- 測試 Public 金鑰：`1A 98 2C 7E 45 9A`

#### 🐛 修復 (Fixed)

- 修正 MainActivity 中 TextView ID 對應問題
- 修正呼叫 Converter 和 CardAnalyzer 的方法名稱

#### 📦 依賴

- targetSdk 更新至 34 (Android 14)
- compileSdk 更新至 34

---

### [2.0.0] - 2025-03-14 - Ultimate Edition

#### ✅ 新增 (Added)

**核心功能**
- ✅ NFC 卡片自動讀取
- ✅ UID 雙重轉換格式（HEX / 10碼 / 8碼）
- ✅ Wiegand 通訊協定轉換（Wiegand26）
- ✅ 卡片類型識別
  - MIFARE Classic / Ultralight / ISO 14443-4
- ✅ 複製風險分析（Clone Analyzer）
- ✅ 50+ 種門禁設備演算法框架

**使用者介面**
- ✅ Material3 設計風格
- ✅ MaterialCardView 卡片佈局
- ✅ Scrollable 介面支援長內容
- ✅ 深藍色主題配色 (#1A237E)
- ✅ 升級版 HR Logo（Vector Drawable）
- ✅ 深色模式支援（自動）

**技術實作**
- ✅ 完整包名重構：`com.helirfid.ultimate`
- ✅ Android X 相容性完整支援
- ✅ Min SDK 23 (Android 6.0)
- ✅ Target SDK 34 (Android 14)
- ✅ Theme.Material3.DayNight.NoActionBar

**程式碼模組**
- ✅ NFCReader.java - NFC UID 讀取
- ✅ Wiegand.java - Wiegand26/34 通訊協定轉換
- ✅ CloneAnalyzer.java - 複製風險偵測
- ✅ CardAnalyzer.java - 卡片類型識別
- ✅ CsvExporter.java - CSV 匯出功能

**專案結構**
- ✅ 完整的 README 說明文檔
- ✅ 貢獻指南 (CONTRIBUTING.md)
- ✅ 變更日誌 (CHANGELOG.md)
- ✅ 開發者指南 (DEVELOPMENT.md)
- ✅ MIT 授權檔案

#### 🔧 變更 (Changed)

**套件重構**
- 從 com.helirfid.app 重構為 com.helirfid.ultimate
- 所有 Java 檔案套件名更新

**依賴更新**
- Material Design：更新至 1.11.0
- compileSdk：更新至 34
- targetSdk：更新至 34

#### ✨ 優化

- 改進 UI 回應速度
- 優化 NFC 讀取穩定性
- 改進錯誤處理

---

### [1.1.0] - 2025-03-14

#### ✅ 新增 (Added)

**功能增強**
- ✅ 卡號自動補滿 10 碼
  - 不足時在前面補零
- ✅ 清除歷史紀錄按鈕
- ✅ 清除歷史記錄後 Toast 提示
- ✅ HistoryManager 清除方法

**使用者介面改進**
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

#### 🔧 變更 (Changed)

**轉換邏輯**
- 卡號現在始終為固定 10 碼（之前可能少於 10 碼）
- 使用前面補零的方式確保固定長度

**使用者介面**
- 按鈕寬度從 `wrap_content` 改為 `match_parent`
- 增加按鈕間距和內邊距
- 歷史紀錄標題改為「歷史紀錄」

#### ✨ 優化

- 改進 UID 轉換算法（補零邏輯）
- HistoryManager 新增 `clear()` 方法
- MainActivity 新增清除按鈕處理邏輯

---

### [1.0.0] - 2025-03-14

#### ✅ 新增 (Added)

初次正式發布 v1.0.0

**核心功能**
- ✅ NFC 卡片自動讀取
- ✅ UID 原始值顯示（16 進制格式）
- ✅ UID 轉門禁卡號功能（後 10 碼）
- ✅ 手動輸入 UID 轉換功能
- ✅ 卡號歷史記錄管理（最多 100 筆）

**使用者介面**
- ✅ 啟動動畫畫面（2 秒延遲）
- ✅ 主畫面佈局 (LinearLayout)
- ✅ 品牌標示顯示："HeliRFID" 和 "禾秝軟體開發團隊"
- ✅ 掃描結果顯示區域
- ✅ 手動輸入欄位
- ✅ 卡號歷史列表

**圖示與設計**
- ✅ HR 品牌圖示（Vector Drawable）
- ✅ 藍色背景設計 (#1565C0)
- ✅ 適配圖示 (Adaptive Icon) 支援
- ✅ 啟動圖示

**技術實作**
- ✅ Android NFC API 整合
- ✅ SharedPreferences 歷史記錄儲存
- ✅ BigInteger 大數運算支援
- ✅ Android X 相容性
- ✅ Min SDK 24 (Android 7.0)
- ✅ Target SDK 33 (Android 13)

**專案結構**
- ✅ Gradle 構建系統
- ✅ 模組化程式碼架構
- ✅ 完整的 README 說明文檔
- ✅ .gitignore 配置
- ✅ MIT 授權檔案
- ✅ 貢獻指南 (CONTRIBUTING.md)
- ✅ 變更日誌 (CHANGELOG.md)

#### 📦 依賴

- androidx.appcompat:appcompat:1.6.1

#### 🐛 修復 (Fixed)

- 修正 Android 12+ 適配問題
  - 添加 `android:exported` 屬性

---

## 📋 版本說明

### 版本編號格式

遵循語義化版本：`MAJOR.MINOR.PATCH`

- **MAJOR**：不相容的 API 變更
- **MINOR**：向下相容的功能性新增
- **PATCH**：向下相容的問題修正

### 變更類型

- **✅ 新增**: 新功能
- **🔧 變更**: 對現有功能的變更
- **🐛 修復**: Bug 修復
- **❌ 移除**: 已移除的功能
- **✨ 優化**: 改進或優化

---

## 🗓 未發布版本

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
- [ ]單元測試覆蓋
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

## 👥 貢獻者

### v1.0.0
- js0935 - 初始開發

---

## 🔗 相關連結

- [HeliRFID GitHub](https://github.com/js0935/HeliRFID)
- [問題追踪](https://github.com/js0935/HeliRFID/issues)
- [貢獻指南](CONTRIBUTING.md)
- [開發者指南](DEVELOPMENT.md)
