# 變更日誌 (Changelog)

本專案的所有重要變更都會記錄在此檔案中。

格式基於 [Keep a Changelog](https://keepachangelog.com/zh-TW/1.0.0/)，
本專案遵守 [語義化版本](https://semver.org/lang/zh-TW/)。

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
