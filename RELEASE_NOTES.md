# HeliRFID 發佈說明

本文檔提供了所有版本的詳細發佈資訊。

---

## 🚀 最新發佈版本

### v3.0.0 - Ultimate Edition (2025-03-14)

**狀態**: ✅ 已發佈

#### 📦 下載資訊

- **APK 檔案**: [HeliRFIDUltimate-v3.0-debug.apk](https://github.com/js0935/HeliRFID/releases/tag/v3.0.0)
- **檔案大小**: 5.3 MB
- **版本日期**: 2025-03-14
- **Git Tag**: [v3.0.0](https://github.com/js0935/HeliRFID/releases/tag/v3.0.0)
- **最低版本**: Android 6.0 (API 23)
- **目標版本**: Android 14 (API 34)

#### ✨ 新功能

1. **Memory Dump 功能**
   - Mifare Classic 記憶體傾印
   - 讀取卡片的記憶體區塊資訊
   - 支援扇區與區塊層級讀取

2. **增強型金鑰測試**
   - 自動測試 5 組常見的 Mifare Classic 金鑰：
     - 工廠預設金鑰：FF FF FF FF FF FF
     - Madroid 金鑰：A0 A1 A2 A3 A4 A5
     - NXP 金鑰：D3 F7 D3 F7 D3 F7
     - Uncommon 金鑰：4D 3A 99 CB 34 0B
     - Public 金鑰：1A 98 2C 7E 45 9A
   - 即時顯示測試成功的金鑰

3. **功能改進**
   - Memory Dump 按鈕在主介面
   - 金鑰測試 results 顯示區域
   - CSV 匯出整合功能

#### 🔧 技術變更

- 將 MainActivity 和 SplashActivity 從 AppCompatActivity 改為 Activity
- 版本號更新：versionCode 3，versionName "3.0"
- 應用程式標籤更新為 "HeliRFID Ultimate v3"
- 啟動畫面更新為 v3.0 版本品牌識別

#### 📱 使用說明

**Memory Dump 使用方式:**
1. 先掃描一張 Mifare Classic 卡片
2. 點擊「Memory Dump」按鈕
3. 系統會讀取卡片資訊並顯示

**金鑰測試說明:**
- 系統會在掃描卡片後自動進行金鑰測試
- 測試結果會顯示在「Key Test」欄位
- 如果測試成功，會顯示可用的金鑰資訊

#### ⚠️ 注意事項

- Memory Dump 功能僅限 Mifare Classic 卡片
- 部分卡片區塊需要正確金鑰才能讀取
- 金鑰測試僅測試常見的 5 組金鑰
- 如果卡片使用自定義金鑰，測試將不會成功

#### 🐛 已知問題

無

#### 🔁 升級建議

從 v2.0.0 升級建議：
- 備份現有歷史記錄（使用 CSV 匯出）
- 卸載舊版本
- 安裝 v3.0.0
- 應用程式資料會保留

---

### v2.0.0 - Ultimate Edition (2025-03-14)

**狀態**: ✅ 已發佈

#### 📦 下載資訊

- **APK 檔案**: [HeliRFIDUltimate-v2.0-debug.apk](https://github.com/js0935/HeliRFID/releases/tag/v2.0.0)
- **檔案大小**: 5.3 MB
- **版本日期**: 2025-03-14
- **Git Tag**: [v2.0.0](https://github.com/js0935/HeliRFID/releases/tag/v2.0.0)
- **最低版本**: Android 6.0 (API 23)
- **目標版本**: Android 14 (API 34)

#### ✨ 新功能

1. **NFC 卡片讀取增強**
   - 自動讀取各種 NFC 卡片 UID
   - 完整支援 MIFARE Classic / Ultralight 系列
   - ISO 14443-4 協定支援

2. **UID 多重格式轉換**
   - HEX 格式（完整 16 進制）
   - 10 碼格式（門禁卡號）
   - 8 碼格式（較短卡號）

3. **Wiegand 通訊協定**
   - Wiegand26 完整支援
   - Wiegand34 框架
   - 格式與奇偶校驗處理
   - 50+ 種門禁設備演算法框架

4. **卡片類型識別**
   - MIFARE Classic (1k, 4k)
   - MIFARE Ultralight
   - ISO 14443 Type A/B
   - NFC-A / B / F / V

5. **複製風險分析**
   - UID 模式偵測
   - 偽造卡片識別
   - 安全性評估

6. **CSV 歷史記錄匯出**
   - 一鍵匯出歷史記錄
   - 儲存至 Downloads 目錄
   - 支援試算表軟體開啟

#### 🎨 使用者介面

- Material3 設計風格
- 深色模式自動支援
- 流暢過場動畫
- 升級版 HR 品牌識別
- MaterialCardView 卡片佈局
- Scrollable 長內容支援

#### 🔧 技術變更

- **套件重構**: com.helirfid.app → com.helirfid.ultimate
- **Material Design**: 更新至 1.11.0
- **Theme**: Theme.Material3.DayNight.NoActionBar
- **compileSdk**: 更新至 34
- **targetSdk**: 更新至 34

#### 📱 新增类別

- NFCReader.java - NFC UID 讀取
- Wiegand.java - Wiegand26/34 通訊協定轉換
- CloneAnalyzer.java - 複製風險偵測
- CardAnalyzer.java - 卡片類型識別
- CsvExporter.java - CSV 匯出功能

#### ⚠️ 注意事項

- 完整重構套件名稱
- 建議全新安裝（而非升級）
- 應用程式資料需要手動備份

#### 🐛 已知問題

無

#### 🔁 升級建議

從 v1.1.0 升級：
- **建議全新安裝**（套件名稱已變更）
- 舊版本資料無法自動遷移
- 可使用 CSV 匯出功能備份舊版本歷史

---

### v1.1.0 (2025-03-14)

**狀態**: ✅ 已發佈

#### 📦 下載資訊

- **APK 檔案**: [HeliRFID-v1.1-debug.apk](https://github.com/js0935/HeliRFID/releases)
- **檔案大小**: 2.9 MB
- **版本日期**: 2025-03-14
- **最低版本**: Android 7.0 (API 24)
- **目標版本**: Android 13 (API 33)

#### ✨ 新功能

1. **卡號自動補滿 10 碼**
   - 不足時在前面補零
   - 固定 10 碼格式

2. **清除歷史紀錄功能**
   - 一鍵清除所有記錄
   - 清除後 Toast 提示

3. **Material Design 風格優化**
   - 藍色標題文字 (#1565C0)
   - 灰色副標題文字 (#666)
   - 淺灰色背景 (#F5F5F5)
   - 白色結果顯示卡片
   - 卡片陰影效果
   - 紅色清除按鈕 (#E53935)

#### 🐛 已知問題

無

---

### v1.0.0 (2025-03-14)

**狀態**: ✅ 已發佈

#### 📦 下載資訊

- **APK 檔案**: [HeliRFID-v1.0-debug.apk](https://github.com/js0935/HeliRFID/releases)
- **檔案大小**: 2.9 MB
- **版本日期**: 2025-03-14
- **最低版本**: Android 7.0 (API 24)
- **目標版本**: Android 13 (API 33)

#### ✨ 初始功能

1. **NFC 卡片讀取**
   - 自動讀取 NFC 卡片 UID
   - 16 進制格式顯示

2. **UID 轉門禁卡號**
   - 後 10 碼轉換
   - 位元組反轉算法

3. **手動輸入功能**
   - 支援帶冒號/不帶冒號格式
   - 即時轉換

4. **歷史記錄管理**
   - 儲存最近 100 筆
   - 自動排序

5. **啟動動畫**
   - 2 秒 Splash 畫面

#### 🎨 使用者介面

- HR 品牌圖示
- 藍色主題色調
- 基礎 Material Design

#### 🐛 已知問題

無

---

## 📋 版本規劃

### 未來版本

#### v3.1.0 (計劃中)

- 支援更多 Mifare 卡片類型
- 支援 DESFire 卡片讀取
- 支援更多門禁協定

#### v4.0.0 (計劃中)

- 多語言支援（繁體中文、簡體中文、英文）
- 智能門禁設備識別
- 雲端備份功能
- 掃描速度優化

---

## 📞 技術支援

### 回報問題

如遇到問題或有功能建議，請：
- 前往 [Issues 頁面](https://github.com/js0935/HeliRFID/issues)
- 搜索是否有相似的問題
- 開啟新 Issue 並詳細描述：
  - 應用程式版本
  - Android 版本
  - 裝置型號
  - 重現步驟
  - 預期行為
  - 實際行為
  - 截圖或錯誤訊息

### 貢獻

我們熱烈歡迎您的貢獻！詳細指南請參閱 [CONTRIBUTING.md](CONTRIBUTING.md)。

---

## 🔗 相關連結

- [GitHub 專案](https://github.com/js0935/HeliRFID)
- [變更日誌](CHANGELOG.md)
- [README.md](README.md)
- [貢獻指南](CONTRIBUTING.md)
- [開發者指南](DEVELOPMENT.md)

---

## 📄 授權

本專案採用 [MIT License](LICENSE) 授權。

---

**Made with ❤️ by [禾秝軟體開發團隊](https://github.com/js0935)**
