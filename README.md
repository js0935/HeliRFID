# HeliRFID - NFC 全功能工具箱

<div align="center">

**禾秝軟體開發團隊**

一款全功能 Android NFC 工具箱應用程式，整合門禁卡讀取、EMV 信用卡、HCE 模擬、任務自動化、標籤分析等 57 項功能

[![Version](https://img.shields.io/badge/version-v4.3.1-blue.svg)](https://github.com/js0935/HeliRFID/releases/latest)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)
[![Android](https://img.shields.io/badge/android-6.0%2B-green.svg)](https://developer.android.com/)

</div>

---

## 什麼是 HeliRFID？

HeliRFID 是一款專為 Android 設計的全功能 NFC 工具箱，提供從基本門禁卡讀取到進階標籤分析、任務自動化等全方位 NFC 功能。

### 核心功能一覽

- **門禁卡讀取**：UID → 10碼/8碼/Wiegand26/34 多格式轉換
- **EMV 信用卡讀取**：PAN/AID/到期日/持卡人 (Visa/Mastercard/Amex/JCB/UnionPay)
- **HCE NDEF 模擬**：將手機模擬為 NDEF Type 4 Tag (文字/網址/電話/簡訊)
- **任務自動化**：280 種動作，NFC 觸發 WiFi/藍牙/音量/App/TTS/Webhook 等
- **標籤分析**：Memory Dump / 存取條件解碼 / 原廠簽章檢查 / 金鑰測試
- **ICODE SLIX**：ISO 15693 AFI 讀寫/DSFID/鎖定
- **NTAG 進階**：密碼/簽章/計數器/格式化
- **背景監控**：持續 NFC 監控 + CSV 日誌 + 桌面小工具
- **NFC 生活工具**：鬧鐘/打卡工時/保險庫/手電筒/媒體控制/TTS
- **工具箱**：9 大類 30+ 項專業工具

---

## 下載與安裝

### 系統需求

| 項目 | 需求 |
|------|------|
| 作業系統 | Android 6.0 (API 23) 或更高版本 |
| NFC | 必須支援 NFC 功能 |
| 儲存空間 | 約 10 MB |

### 下載 APK

**最新版本：v4.3.1 NFC 全功能工具箱版** (2026-06-21)

[📥 下載 HeliRFID-v4.3.1-release.apk](https://github.com/js0935/HeliRFID/releases/download/v4.3.1/HeliRFID-v4.3.1-release.apk) (3.5 MB)

### 安裝步驟

1. 下載 APK 檔案
2. 在手機上開啟 APK
3. 允許「來源不明應用程式」安裝
4. 完成安裝並開啟

---

## 快速開始

1. **開啟 NFC**：前往設定 → NFC → 開啟
2. **掃描卡片**：將 NFC 卡片靠近手機背面 (1-3 公分)
3. **查看結果**：UID、10碼、8碼、Wiegand 等多格式顯示
4. **探索工具箱**：從主畫面點擊「工具箱」進入 30+ 功能

---

## HCE 標籤模擬使用說明

HeliRFID 支援 **Host Card Emulation (HCE)**，可將手機模擬為 **NDEF Type 4 Tag**，讓其他 NFC 讀取器讀取您自訂的文字、網址、電話或簡訊內容。

### 使用步驟

1. 從主畫面點擊「HCE 標籤模擬」進入
2. **設定模擬內容**：選擇類型（文字/網址/電話/簡訊）並填入內容
3. **掃描實體卡片儲存設定檔**（選用）：點擊「掃描卡片」讀取 NFC 標籤資訊，可儲存為模擬設定檔（最多 10 筆）
4. **開啟模擬**：點擊「啟動模擬」按鈕，手機即開始模擬 NFC 標籤
5. **將手機靠近讀取器**：另一台 NFC 裝置或讀卡器靠近手機背面即可讀取到設定的 NDEF 內容

### 功能特點

- **多筆設定檔管理**：支援儲存最多 10 筆模擬設定檔，可選取刪除
- **NDEF 內容自動復原**：重新開啟應用程式後自動載入上次儲存的 NDEF 內容
- **FCI/CC 完整模擬**：符合 ISO 7816-4 標準的 File Control Information 與 Capability Container 回應
- **支援多種 NDEF 格式**：文字 (TEXT)、網址 (URI)、電話 (TEL)、簡訊 (SMS)
- **Foreground Dispatch**：使用標準 NFC 前台分派系統，相容性最高

### 技術細節

| 項目 | 說明 |
|------|------|
| 模擬標準 | ISO 14443-4 / NDEF Type 4 Tag |
| AID | D2760000850101 (NDEF) + D2760000850100 (非 NDEF) |
| CC 檔案 | `00 0E 20 FF FF 04 06 E1 04 [size] 00 FF 00` (14 bytes) |
| FCI 結構 | 6F 13 84 07 D2760000850101 A5 08 06 02 20 FF 5F55 01 FE |
| 最大 NDEF 大小 | 動態計算，依據 CC 檔案宣告 |
| 寫入權限 | 唯讀 (0xFF)，不支援寫入 |

---

## 工具箱功能分類

### NDEF 與 QR 工具
- NDEF 編輯器 / NDEF 驗證器 / QR 掃描器 / QR 顯示
- NFC2QR / 批次寫入 (CSV 匯入)

### 標籤讀寫
- 寫入標籤 / 標籤資訊 / 格式化 / 鎖定 / 克隆
- 標籤盤點 / 標籤週期 / 標籤歷史

### 安全與金鑰
- 金鑰管理 / 進階金鑰模板 / 金鑰恢復 / 存取條件解碼
- 金鑰測試 (5 種常見金鑰集)

### 金融卡與識別
- EMV 信用卡讀取 / 卡片指紋辨識 / 原廠簽章檢查
- 卡片分析 (SAK/ATQA/技術類型)

### 分析與 Dump 工具
- Memory Dump / Dump 編輯器 / Dump 比對 / Dump ASCII
- Dump 色彩高亮 / 數值區塊編解碼 / 驗證地圖 / APDU 控制台

### 任務自動化
- 工作自動化 / 條件任務 / 檔案操作任務 / 任務變數
- TaskExecutor (280 種動作) / PlaceholderEngine

### 模擬與通訊
- HCE NDEF 模擬 / HCE 標籤模擬 / NFC 中繼 / VAS Reader
- ACR122U 外接讀卡器 / ISO 15693 / DESFire / FeliCa

### 監控與日誌
- NFC 背景監控 / NFC 監聽器 / NFC 輪詢 / 訊號強度
- 掃描日誌 / 日誌檢視器 / 報告匯出 / 桌面小工具

### NFC 生活工具
- NFC 鬧鐘 / NFC 打卡工時 / NFC 保險庫 / NFC Webhook
- TTS 語音朗讀 / 手電筒 / 媒體控制 / App 封鎖
- 輔助使用切換 / 智慧設定檔 / 預設設定檔

---

## 版本資訊

### v4.3.1 (2026-06-21) - NFC 全功能工具箱版

**16+ 項新功能：**
- HCE NDEF 模擬 (文字/網址/電話/簡訊)
- NFC 背景監控 + CSV 日誌 + 桌面小工具
- TaskExecutor 擴充 200→280 動作
- PlaceholderEngine 佔位符系統
- TTS/手電筒/媒體控制/NFC 保險庫/標籤盤點/週期計數
- NFC Webhook/鬧鐘/打卡工時/NFC2QR
- 輔助使用切換/App 封鎖/設定檔管理/報告匯出
- ICODE SLIX (ISO 15693) / 原廠簽章檢查
- 批次寫入強化 (CSV + PlaceholderEngine)
- 工具箱 9 大類重新分類
- BaseNfcActivity Reader Mode 根除系統對話框

### v4.3.0 - EMV & Advanced NTAG Edition
EMV 信用卡、NTAG 進階工具、廠商區塊寫入、NFC 流量監聽

查看完整變更：[CHANGELOG.md](CHANGELOG.md)

---

## 從原始碼編譯

```bash
git clone https://github.com/js0935/HeliRFID.git
cd heli_rfid_nfc
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

詳細開發資訊請參閱 [DEVELOPMENT.md](DEVELOPMENT.md)

---

## 技術資訊

| 技術 | 版本 |
|------|------|
| 開發語言 | Java 8 |
| 最低 SDK | Android 6.0 (API 23) |
| 目標 SDK | Android 14 (API 34) |
| Gradle | 8.0 |
| AGP | 8.1.0 |
| Material Design | 1.11.0 |

### 專案規模

- Java 檔案：90+ 個
- Activities：57 個
- Services：3 個
- Receivers/Providers：5+ 個
- 工具箱功能：30+ 項
- 任務自動化動作：280 種

---

## 常見問題

**Q: 應用程式免費嗎？**
A: 是的，HeliRFID 完全免費且開源 (MIT License)。

**Q: 需要網路連線嗎？**
A: 大部分功能離線運作，Webhook/對外 IP 功能需要網路。

**Q: 支援哪些卡片？**
A: MIFARE Classic/Ultralight、NTAG 系列、DESFire、FeliCa、ISO 14443 A/B、ISO 15693、EMV 信用卡

**Q: 應用程式會上傳資料嗎？**
A: 不會，所有資料僅儲存在您的手機上。

---

## 貢獻與支援

我們歡迎您的貢獻！
- Fork 專案並提交 Pull Request
- 回報問題：[GitHub Issues](https://github.com/js0935/HeliRFID/issues)

詳細指南請參閱 [CONTRIBUTING.md](CONTRIBUTING.md)

---

## 授權

本專案採用 [MIT License](LICENSE) 授權。

```
Copyright (c) 2025-2026 禾秝軟體開發團隊 (HeliRFID Development Team)
```

---

<div align="center">

**Made with ❤️ by [禾秝軟體開發團隊](https://github.com/js0935)**

[⬆ 回到頂部](#helirfid---nfc-全功能工具箱)

⭐ 如果喜歡這個專案，請給我們一顆星星！

</div>