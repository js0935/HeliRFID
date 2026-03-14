# HeliRFID 快速開始指南

歡迎使用 HeliRFID！本指南將幫助您在幾分鐘內開始使用。

---

## 📱 什么是 HeliRFID？

HeliRFID 是一款專為 Android 設計的 NFC 門禁卡讀取工具，可以：
- 📖 讀取各類 NFC 卡片
- 🔢 自動轉換卡號格式（10 碼 / 8 碼）
- 🔐 支援 Wiegand 門禁協定
- 📊 檢視卡片記憶體內容
- 📝 管理掃描歷史
- 📤 匯出 CSV 檔案

---

## ⚡ 5 分鐘快速開始

### 步驟 1: 檢查您的手機

確保您的手機符合以下條件：
- ✅ **系統版本**：Android 6.0 或更高版本
- ✅ **NFC 功能**：手機必須支援 NFC
- ✅ **儲存空間**：至少 10 MB 可用空間

**如何檢查 NFC：**
1. 前往「設定」
2. 搜尋或找到「NFC」設置
3. 確保 NFC 已開啟

### 步驟 2: 下載並安裝

**最新版本：v4.0.1**

[📥 下載 HeliRFIDProfessional-v4.0.1-debug-fixed.apk](https://github.com/js0935/HeliRFID/raw/refs/tags/v4.0.1/releases/HeliRFIDProfessional-v4.0.1-debug-fixed.apk)

**安裝方法：**
1. 點擊下載連結
2. 等待下載完成
3. 在手機上開啟 APK 檔案
4. 如果提示「安裝未知來源應用程式」，請點擊「允許」或「設定」並啟用
5. 完成安裝
6. 在應用程式列表中找到「HeliRFID 專業版」並開啟

### 步驟 3: 第一次使用

啟動應用程式後，您會看到：

```
1.啟動畫面（2 秒）
   ↓
2.主畫面顯示「HeliRFID 專業版」
   ↓
3.提示「請將 NFC 卡靠近手機」
```

### 步驟 4: 掃描卡片

1. 將 NFC 卡片放在手機背面
2. 保持距離 1-3 公分
3. 等待讀取完成（通常 1-2 秒）

**您會看到：**
```
UID: 04:A2:B3:C4:D5:E6:78
10碼: 2029985476
8碼: 5476
Wiegand26: 0000-78E6-D5C4-0000
Card Type: MIFARE Classic
```

---

## 🎯 主要功能說明

### 1. 卡號轉換

HeliRFID 自動將 UID 轉換為多種格式：

| 格式 | 說明 | 使用場景 |
|------|------|----------|
| **UID 原始值** | 卡片的唯一識別碼 | 一般識別 |
| **10 碼** | 10 位數字卡號 | 大部分門禁系統 |
| **8 碼** | 8 位數字卡號 | 較短的卡號需求 |
| **Wiegand26** | 門禁通訊協定 | 專業門禁系統 |

### 2. 歷史記錄

- 自動儲存最近 100 筆掃描記錄
- 最新記錄顯示在最上方
- 可隨時查看歷史
- 一鍵清除所有記錄

### 3. CSV 匯出

將歷史記錄匯出為 CSV 檔案：

1. 點點擊「匯出 CSV」按鈕
2. 檔案儲存在「下載」目錄
3. 檔名：`HeliRFID_History.csv`
4. 可用於 Excel 或其他試算表軟體

### 4. Memory Dump

檢視 NFC 卡片的記憶體內容：

1. 先掃描一張卡片
2. 點擊「Memory Dump」按鈕
3. 以表格形式查看扇區和區塊資訊

**適用於：**
- MIFARE Classic 卡片
- 需要記憶體存取的卡片
- 部分區塊可能需要正確的金鑰

---

## ❓ 常見問題速查

### 安裝問題

**Q: 無法安裝 APK**
A:
- 檢查是否允許「來源不明應用程式」安裝
- 確保下載的檔案完整
- 嘗試重新下載

**Q: 安裝後找不到應用程式**
A:
- 在應用程式列表中搜尋「HeliRFID」
- 檢查是否安裝成功

### 使用問題

**Q: NFC 不讀取**
A:
- 檢查 NFC 是否已開啟
- 確認卡片與手機距離（1-3 公分）
- 嘗試稍微移動卡片位置
- 某些卡片需要特定位置

**Q: 轉換結果不正確**
A:
- 確認使用的卡片類型正確
- 部分特殊卡片可能不支援轉換
- 嘗試重新掃描

**Q: CSV 檔案在哪裡？**
A:
- 開啟「檔案管理器」應用程式
- 前往「下載」目錄
- 查找 `HeliRFID_History.csv`

**Q: 如何清除歷史記錄？**
A:
- 點擊「清除紀錄」按鈕
- 確認清除操作
- 所有歷史記錄將被刪除

### 進階問題

**Q: 支援哪些卡片？**
A:
- MIFARE Classic (1k, 4k)
- MIFARE Ultralight
- ISO 14443 Type A/B
- FeliCa 卡片
- 其他 13.56 MHz NFC 卡片

**Q: 需要網路連線嗎？**
A:
- 不需要
- 所有功能都在離線狀態下運作

**Q: 資料會上傳嗎？**
A:
- 不會
- 所有資料儲存在您的手機上
- 不會發送到任何伺服器

---

## 🔒 隱私與權限

### 需要的權限

1. **NFC 權限**
   - 用途：讀取 NFC 卡片
   - 必要性：核心功能必需

2. **儲存空間權限**
   - 用途：匯出 CSV 檔案
   - 必要性：匯出功能必需

### 隱私政策

- 所有資料僅儲存在您的裝置上
- 不會上傳任何資料到外部伺服器
- 不會收集任何個人識別資訊
- 掃描記錄完全由您控制

---

## 📖 進階使用

### 從原始碼編譯

如果您想自行編譯應用程式：

```bash
# 確保已安裝 Java JDK 和 Android SDK

# 複製專案
git clone https://github.com/js0935/HeliRFID.git
cd heli_rfid_nfc

# 編譯
./gradle-8.0/bin/gradle assembleDebug

# APK 位置
app/build/outputs/apk/debug/app-debug.apk
```

詳細資訊請參閱 [DEVELOPMENT.md](DEVELOPMENT.md)

### 貢獻與反饋

我們歡迎您的意見和建議：

- **回報 Bug**: [GitHub Issues](https://github.com/js0935/HeliRFID/issues)
- **功能建議**: [GitHub Issues](https://github.com/js0935/HeliRFID/issues)
- **貢獻程式碼**: [CONTRIBUTING.md](CONTRIBUTING.md)

---

## 🆘 獲得幫助

如果仍有問題：

1. 查看 [完整常見問題](../README.md#-常見問題)
2. 閱讀 [開發者指南](DEVELOPMENT.md)
3. 開啟 [GitHub Issue](https://github.com/js0935/HeliRFID/issues)
4. 聯絡我們：[js0935](https://github.com/js0935)

---

## 📱 版本資訊

**當前版本：v4.0.1**
- 發布日期：2025-03-14
- Android 版本：6.0+
- APK 大小：5.5 MB

### 最新修復

- ✅ 修復版本顯示問題
- ✅ 修復 CSV 匯出功能（Android 10+）
- ✅ 修復啟動畫面標題

查看完整變更：[CHANGELOG.md](CHANGELOG.md)

---

開始使用 HeliRFID，輕鬆讀取您所有的 NFC 卡片！🚀

<div align="center">

**Made with ❤️ by [禾秝軟體開發團隊](https://github.com/js0935)**

[⬆ 回到頂部](#helifid-快速開始指南)

</div>
