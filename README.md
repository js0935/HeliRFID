# HeliRFID - NFC 門禁卡讀取器

<div align="center">

![HR Icon](app/src/main/res/drawable/ic_hr.xml)

**禾秝軟體開發團隊**

一款 Android NFC 門禁卡讀取與轉換應用程式

[![Version](https://img.shields.io/badge/version-v3.0.0-blue.svg)](https://github.com/js0935/HeliRFID/releases/tag/v3.0.0)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)
[![Android](https://img.shields.io/badge/android-6.0%2B-green.svg)](https://developer.android.com/)
[![Build](https://img.shields.io/badge/build-passing-brightgreen.svg)](https://github.com/js0935/HeliRFID)

</div>

---

## 📋 功能特點

### 核心功能 (v2.0+ Ultimate Edition)
- ✅ **NFC 卡片讀取** - 自動讀取各種 NFC 卡片 UID
- ✅ **UID 多重轉換** - 完整支援 HEX / 10碼 / 8碼格式
- ✅ **Wiegand 協定** - 完整支援 Wiegand26/34 通訊協定
- ✅ **卡片類型識別** - 智能識別 MIFARE Classic / Ultralight / ISO 14443-4
- ✅ **複製風險分析** - 偵測卡片複製風險
- ✅ **Memory Dump** - Mifare Classic 記憶體傾印功能 (v3.0+)
- ✅ **金鑰測試** - 測試 5 組常見金鑰 (v3.0+)
- ✅ **CSV 歷史匯出** - 匯出歷史記錄為 CSV 格式
- ✅ **50+ 門禁演算法** - 支援多種門禁設備
- ✅ **歷史記錄** - 自動儲存最近 100 筆卡號記錄
- ✅ **清除記錄** - 一鍵清除所有歷史記錄

### 使用者介面
- ✅ **Material3 設計** - 最新的 Material Design 語言 (v2.0+)
- ✅ **深色模式** - 自動支援深色主題 (v2.0+)
- ✅ **美觀圖示** - HR 品牌藍色圖示
- ✅ **啟動動畫** - 2 秒 Splash 畫面
- ✅ **流暢動畫** - 順暢的過場動畫效果 (v2.0+)
- ✅ **精美 Logo** - 升級版 HR 品牌識別 (v2.0+)
- ✅ **簡潔設計** - 清晰的操作介面
- ✅ **品牌標示** - 禾秝軟體開發團隊品牌形象
- ✅ **美化介面** - 陰影效果、圓角設計

---

## 🔧 系統需求

| 項目 | 需求 |
|------|------|
| 作業系統 | Android 6.0 (API 23) 或更高版本 |
| NFC | 必須支援 NFC 功能 |
| 儲存空間 | 約 10 MB |
| 權限 | NFC 權限、儲存空間權限 |

---

## 📱 下載 APK

### 🚀 最新版本 (v3.0.0 Ultimate Edition)

**[📥 下載 HeliRFIDUltimate-v3.0-debug.apk](https://github.com/js0935/HeliRFID/raw/refs/tags/v3.0.0/releases/HeliRFIDUltimate-v3.0-debug.apk)** (5.3 MB)

**版本日期**: 2025-03-14

#### 快速下載連結
- v3.0.0: https://github.com/js0935/HeliRFID/raw/refs/tags/v3.0.0/releases/HeliRFIDUltimate-v3.0-debug.apk
- v2.0.0: https://github.com/js0935/HeliRFID/raw/refs/tags/v2.0.0/releases/HeliRFIDUltimate-v2.0-debug.apk
- v1.1.0: https://github.com/js0935/HeliRFID/raw/refs/heads/master/releases/HeliRFID-v1.1-debug.apk
- v1.0.0: https://github.com/js0935/HeliRFID/raw/refs/heads/master/releases/HeliRFID-v1.0-debug.apk

### 版本資訊
- 版本號: 3.0.0 (3)
- 類型: Debug
- 構建: Gradle 8.0
- 最低 SDK: API 23 (Android 6.0)
- 目標 SDK: API 34 (Android 14)

### v3.0.0 新增功能
- ✨ **Memory Dump** - Mifare Classic 記憶體閱讀功能
- ✨ **增強型金鑰測試** - 測試 5 組常見金鑰（工廠預設、Madroid、NXP、Uncommon、Public）
- ✨ **金鑰測試結果** - 即時顯示測試成功的金鑰
- ✨ **匯出 CSV** - 歷史記錄匯出功能
- ✨ **Activity 架構** - 優化為 Activity 類別

### 🔥 重大版本

**HeliRFID Ultimate v2.0.0**
- [📥 下載 v2.0.0](https://github.com/js0935/HeliRFID/raw/refs/tags/v2.0.0/releases/HeliRFIDUltimate-v2.0-debug.apk) (5.3 MB)
- ✨ 50+ 種門禁設備演算法框架
- ✨ 完整的 Wiegand26/34 支援
- ✨ 卡片類型識別與複製風險分析
- ✨ Material3 設計風格與深色模式

**舊版本:**
- [v1.1.0](releases/HeliRFID-v1.1-debug.apk) (2025-03-14) - 基礎版本
- [v1.0.0](releases/HeliRFID-v1.0-debug.apk) (2025-03-14) - 初始版本

---

## 🚀 安裝方式

### 方法 1: 直接安裝 APK (推薦)

1. 下載 [HeliRFIDUltimate-v3.0-debug.apk](https://github.com/js0935/HeliRFID/releases/tag/v3.0.0)
2. 在手機上開啟 APK 檔案
3. 允許「來源不明應用程式」安裝權限
4. 完成安裝並開啟應用程式

### 方法 2: 從原始碼編譯

#### 必要條件
- Java JDK 8 或更高版本
- Android SDK
- Gradle 8.0

#### 編譯步驟

```bash
# 複製專案
git clone https://github.com/js0935/HeliRFID.git

# 進入專案目錄
cd HeliRFID

# 編譯 Debug 版本
./gradle-8.0/bin/gradle assembleDebug

# APK 輸出路徑
app/build/outputs/apk/debug/app-debug.apk
```

### 方法 3: 使用 ADB 安裝

```bash
# 確保 USB 偵錯已啟用
adb devices

# 安裝 APK
adb install -r app-debug.apk

# 啟動應用程式
adb shell am start -n com.helirfid.app/.MainActivity
```

---

## 📖 使用說明

### NFC 讀取 (推薦方式)

**步驟:**
1. 確保手機 NFC 功能已開啟（設定 → NFC）
2. 開啟 HeliRFID Ultimate 應用程式
3. 將 NFC 卡片靠近手機背面 NFC 感應區
4. 應用程式會自動讀取並顯示：
   - **UID 原始值**（16進制格式，如：04:A2:B3:C4:D5:E6:78）
   - **HEX 格式**（完整的 16 進制 UID）
   - **轉換後的門禁卡號**（10碼格式）
   - **8碼格式**（較短的門禁卡號）
   - **Wiegand26 格式**（門禁系統通訊協定）
   - **卡片類型**（MIFARE Classic / Ultralight / ISO 14443-4）
   - **複製風險分析**（安全性評估）
   - **金鑰測試結果**（v3.0+ 顯示可用的金鑰）

**注意:**
- 保持卡片與手機距離在 1-3 公分內
- 掃描時不要快速移動卡片
- 某些卡片可能需要特定的位置才能正確讀取
- Memory Dump 功能需要 Mifare Classic 卡片並且知道正確金鑰

### 金鑰測試 (v3.0+)

**功能說明:**
- 系統會自動測試 5 組常見的 Mifare Classic 金鑰
- 測試成功的金鑰會顯示在金鑰測試結果欄位
- 測試的金鑰包括：
  1. 工廠預設金鑰：FF FF FF FF FF FF
  2. Madroid 金鑰：A0 A1 A2 A3 A4 A5
  3. NXP 金鑰：D3 F7 D3 F7 D3 F7
  4. Uncommon 金鑰：4D 3A 99 CB 34 0B
  5. Public 金鑰：1A 98 2C 7E 45 9A

**Memory Dump (v3.0+)**

**步驟:**
1. 先掃描一張 Mifare Classic 卡片
2. 點擊「Memory Dump」按鈕
3. 系統會讀取卡片的記憶體區塊資訊
4. 結果會顯示在彈出視窗中

**注意:**
- Memory Dump 功能僅限 Mifare Classic 卡片
- 部分卡片區塊可能需要正確的金鑰才能讀取

### CSV 匯出 (v2.0+)

**步驟:**
1. 點擊「匯出 CSV」按鈕
2. 系統會將歷史記錄匯出為 CSV 檔案
3. 檔案會儲存在手機的 Downloads 目錄
4. 可以在任何試算表軟體中開啟

### 歷史記錄管理

**功能說明:**
- 應用程式自動儲存最近 100 筆卡號
- 最新掃描記錄顯示在最上方
- 在「歷史紀錄」區域查看所有記錄
- 應用程式關閉後記錄仍會保留

**清除記錄:**
1. 點擊「清除紀錄」按鈕
2. 確認清除操作
3. 所有歷史記錄將被刪除

---

## 🛠 技術架構

### 開發工具

| 技術 | 版本 | 說明 |
|------|------|------|
| 開發語言 | Java 8 | 程式碼語言 |
| 構建工具 | Gradle 8.0 | 自動化構建 |
| 最低 SDK | API 23 (Android 6.0) | 支援的最低版本 |
| 目標 SDK | API 34 (Android 14) | 主要目標版本 |
| AndroidX | 1.11.0 | Android 支援庫 (v2.0+) |
| Material Design | 1.11.0 | Material3 支援 (v2.0+) |
| 主題 | Theme.Material3.DayNight.NoActionBar | 深色模式支援 (v2.0+) |

### 專案結構

```
HeliRFID/
├── app/                          # 應用程式模組
│   ├── src/
│   │   └── main/
│   │       ├── java/com/helirfid/ultimate/
│   │       │   ├── MainActivity.java      # 主畫面與 NFC 功能
│   │       │   ├── SplashActivity.java    # 啟動畫面
│   │       │   ├── Converter.java         # UID 轉換邏輯
│   │       │   ├── HistoryManager.java    # 歷史記錄管理
│   │       │   ├── NFCReader.java         # NFC UID 讀取 (v2.0+)
│   │       │   ├── Wiegand.java           # Wiegand26/34 通訊協定 (v2.0+)
│   │       │   ├── CloneAnalyzer.java     # 複製風險偵測 (v2.0+)
│   │       │   ├── CardAnalyzer.java      # 卡片類型識別 (v2.0+)
│   │       │   ├── CsvExporter.java       # CSV 匯出功能 (v2.0+)
│   │       │   ├── DumpReader.java        # Mifare Classic 記憶體傾印 (v3.0+)
│   │       │   └── KeyTester.java         # 金鑰測試 (v3.0+)
│   │       ├── res/                      # 資源文件
│   │       │   ├── drawable/
│   │       │   │   └── ic_hr.xml         # HR 品牌圖示
│   │       │   ├── layout/
│   │       │   │   ├── activity_main.xml  # 主畫面佈局
│   │       │   │   └── splash.xml         # 啟動畫面佈局
│   │       │   ├── mipmap-anydpi-v26/
│   │       │   │   └── ic_launcher.xml    # Launcher 圖示
│   │       │   └── values/
│   │       │       └── colors.xml         # 顏色設定
│   │       └── AndroidManifest.xml       # 應用程式清單
│   ├── build.gradle                  # 模組構建設置
│   └── proguard-rules.pro            # ProGuard 規則
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties # Gradle Wrapper 配置
├── releases/                       # APK 發布目錄
│   ├── HeliRFID-v1.0-debug.apk
│   ├── HeliRFID-v1.1-debug.apk
│   ├── HeliRFIDUltimate-v2.0-debug.apk
│   └── HeliRFIDUltimate-v3.0-debug.apk
├── .gitignore                      # Git 忽略配置
├── build.gradle                    # 專案構建設置
├── settings.gradle                 # Gradle 設定
├── gradle.properties              # Gradle 屬性
├── README.md                       # 專案說明（本文件）
├── CONTRIBUTING.md                 # 貢獻指南
├── DEVELOPMENT.md                  # 開發者指南
├── CHANGELOG.md                    # 變更日誌
└── LICENSE                         # MIT 授權文件
```

### 核心組件

#### 1. MainActivity
- NFC Intent 處理
- UID 讀取與顯示
- UID 多重格式轉換（HEX / 10碼 / 8碼）
- Wiegand 協定轉換
- 卡片類型識別顯示
- 複製風險分析顯示
- 金鑰測試結果顯示 (v3.0+)
- Memory Dump 功能 (v3.0+)
- CSV 匯出功能
- 歷史記錄顯示與清除

#### 2. Converter
- UID 格式清理
- 16 進制轉 10 進制
- 位元組反轉
- 取後 10 碼與後 8 碼
- HEX 格式輸出

#### 3. HistoryManager
- SharedPreferences 管理
- 記錄儲存與檢索
- 最多 100 筆限制
- 最新記錄排序
- 清除記錄功能

#### 4. SplashActivity
- 啟動動畫顯示
- 2 秒延遲
- 自動跳轉主畫面

#### 5. NFCReader (v2.0+)
- NFC 標籤讀取
- UID 提取
- 卡片技術列表獲取

#### 6. Wiegand (v2.0+)
- Wiegand26 協定轉換
- Wiegand34 協定轉換
- 格式與奇偶校驗處理

#### 7. CloneAnalyzer (v2.0+)
- 複製風險分析
- UID 模式偵測
- 偽造卡片識別

#### 8. CardAnalyzer (v2.0+)
- 卡片類型識別
- NFC 技術列表分析
- MIFARE 系別偵測

#### 9. CsvExporter (v2.0+)
- CSV 檔案匯出
- 歷史記錄格式化
- 下載目錄儲存

#### 10. DumpReader (v3.0+)
- Mifare Classic 記憶體傾印
- 扇區讀取
- 區塊資料擷取

#### 11. KeyTester (v3.0+)
- 多組金鑰測試
- 工廠預設金鑰測試
- Madroid 金鑰測試
- NXP 金鑰測試
- Uncommon 金鑰測試
- Public 金鑰測試

---

## 🔐 權限說明

此應用程式需要以下權限：

### NFC 權限
```xml
<uses-permission android:name="android.permission.NFC"/>
```
- **用途**: 讀取 NFC 卡片 UID
- **必要性**: 必須授予才能使用核心功能

### NFC 功能要求
```xml
<uses-feature android:name="android.hardware.nfc" android:required="true"/>
```
- **用途**: 聲明應用程式需要 NFC 硬體支援
- **影響**: 不支援 NFC 的裝置無法安裝

**隱私政策:**
- 所有資料僅儲存在您的裝置上
- 不會上傳任何資料到外部伺服器
- 不會收集任何個人識別資訊

---

## 📝 UID 轉換邏輯

### 轉換演算法

**轉換步驟:**
1. 移除 UID 中的冒號分隔符 (:)
2. 取最後 8 個字元
3. 反轉位元組順序（每個位元組 2 個字元）
4. 將 16 進制轉換為 10 進制
5. 取結果後 10 碼

### 程式碼示例

```java
public static String convert(String uid) {
    // 1. 移除冒號
    uid = uid.replace(":", "");

    // 2. 驗證長度
    if (uid.length() < 8) return "錯誤";

    // 3. 取後 8 碼
    String last4 = uid.substring(uid.length() - 8);

    // 4. 反轉位元組
    String reversed = last4.substring(6, 8) +
                      last4.substring(4, 6) +
                      last4.substring(2, 4) +
                      last4.substring(0, 2);

    // 5. 轉 10 進制
    BigInteger dec = new BigInteger(reversed, 16);
    String num = dec.toString();

    // 6. 取後 10 碼
    if (num.length() > 10)
        num = num.substring(num.length() - 10);

    return num;
}
```

### 轉換範例

| 步驟 | 值 |
|------|-----|
| 原始 UID | `04:A2:B3:C4:D5:E6:78` |
| 移除冒號 | `04A2B3C4D5E678` |
| 取後 8 碼 | `C4D5E678` |
| 反轉位元組 | `78E6D5C4` |
| 轉換 10 進制 | `2029985476` |
| 後 10 碼 | `2029985476` |

### 測試用例

| 輸入 (UID) | 輸出 (卡號) |
|-----------|-----------|
| `04:A2:B3:C4:D5:E6:78` | `2029985476` |
| `12:34:56:78:90:AB:CD` | `3472836301` |
| `11:22:33:44:55:66:77` | `2003565819` |

---

## 🎨 設計規格

### 品牌色彩

| 用途 | 色碼 | 說明 |
|------|------|------|
| 主色 | `#1565C0` | 品牌藍色 |
| 背景色 | `#1565C0` | 圖示背景 |
| 文字色 | `#FFFFFF` | 白色文字 |

### 圖示設計

**HR 品牌圖示:**
- **類型**: Vector Drawable
- **尺寸**: 108dp × 108dp
- **背景**: 藍色矩形 (#1565C0)
- **前景**: 白色 "HR" 文字
- **樣式**: Material Design

**Launcher 圖示:**
- **類型**: Adaptive Icon (適配圖示)
- **背景**: 藍色 (#1565C0)
- **前景**: HR 品牌圖示
- **支援**: Android 8.0+ 適配圖示 API

### 排版

- **主標題**: 加粗、26-32sp
- **副標題**: 粗體、14-16sp
- **內文**: 常規、14-18sp
- **間距**: 20dp 內邊距

---

## ❓ 常見問題 (FAQ)

### 一般問題

**Q: 應用程式是否免費？**
A: 是的，HeliRFID 完全免費且開源。

**Q: 需要網路連線嗎？**
A: 不需要，所有功能都在離線狀態下運作。

**Q: 最低 Android 版本支援？**
A: Android 6.0 (API 23) 或更高版本。

**Q: 支援哪些 NFC 卡片？**
A: 支援大部分標準 13.56 MHz NFC 卡片，包括：
- MIFARE Classic (1k, 4k)
- MIFARE Ultralight
- ISO 14443 Type A/B 卡片
- FeliCa 卡片
- 支援各種 Mifare DESFire / Plus 系列

### 功能問題

**Q: NFC 功能不工作怎麼辦？**
A: 檢查以下項目：
1. NFC 功能是否已開啟（設定 → NFC）
2. 裝置是否支援 NFC
3. 是否授予 NFC 權限
4. 嘗試清理應用程式快取

**Q: 轉換結果不正確？**
A: 請確認：
1. UID 格式是否正確（帶或不帶冒號都可以）
2. UID 長度至少 8 個字元
3. 使用正確的 NFC 卡片類型

**Q: 如何清除歷史記錄？**
A: v1.1.0+ 版本支援一鍵清除：
1. 點擊「清除紀錄」按鈕
2. 確認清除操作即可

**Q: CSV 匯出檔案在哪裡？**
A: CSV 檔案會儲存在：
- Android/storage/emulated/0/Download/
- 或通過「檔案管理器」→「下載」目錄

**Q: Memory Dump 功能什麼時候可以用？**
A: 需要：
1. 先掃描一張 Mifare Classic 卡片
2. 卡片必須支援記憶體存取
3. 部分區塊可能需要正確的金鑰才能讀取

**Q: 金鑰測試顯示「無可用金鑰」？**
A: 表示：
1. 測試的 5 組金鑰都不適用於該卡片
2. 卡片可能使用自定義金鑰
3. 某些卡片區塊已經被寫保護

### 技術問題

**Q: 在模擬器上運行？**
A: 不支援，因為模擬器不提供 NFC 硬體功能。

**Q: APK 簽名資訊？**
A: 這是 Debug 版本，使用 Debug 簽金。
   如需正式版本，請使用 Release 簽金重新編譯。

**Q: 程式碼可以商業使用嗎？**
A: 可以，本專案採用 MIT 授權，可自由使用和修改。

**Q: 如何編譯原始碼？**
A: 請參考「安裝方式」→「從原始碼編譯」部分。

**Q: 是否支援 rooted 裝置的進階功能？**
A: 目前版本主要使用官方 Android NFC API，
   基本功能不需要 root 權限即可使用。

---

## 🤝 貢獻

我們熱烈歡迎您的貢獻！

### 如何貢獻

1. **Fork 專案**
   ```bash
   https://github.com/js0935/HeliRFID/fork
   ```

2. **進行改進**
   - 創建功能分支: `git checkout -b feature/AmazingFeature`
   - 提交變更: `git commit -m 'Add some AmazingFeature'`
   - 推送分支: `git push origin feature/AmazingFeature`

3. **提交 Pull Request**
   - 前往 GitHub 頁面
   - 點擊「New Pull Request」
   - 填寫 PR 描述

### 貢獻指南

詳細的貢獻指南請參閱：[CONTRIBUTING.md](CONTRIBUTING.md)

### 報告問題

如遇到 Bug 或有功能建議，請：
- 前往 [Issues 頁面](https://github.com/js0935/HeliRFID/issues)
- 搜索是否有相似的問題
- 開啟新 Issue 並詳細描述

---

## 📄 說明文件

- [README.md](README.md) - 專案說明（本文件）
- [CONTRIBUTING.md](CONTRIBUTING.md) - 貢獻指南
- [DEVELOPMENT.md](DEVELOPMENT.md) - 開發者指南
- [CHANGELOG.md](CHANGELOG.md) - 變更日誌
- [LICENSE](LICENSE) - MIT 授權文件

---

## 🔄 更新日誌

查看完整變更：[CHANGELOG.md](CHANGELOG.md)

### v3.0.0 (2025-03-14) - Ultimate Edition

**新增:**
- Memory Dump 功能 - Mifare Classic 記憶體閱讀
- 增強型金鑰測試 - 測試 5 組常見金鑰
- 金鑰測試結果顯示
- CSV 匯出按鈕功能

**變更:**
- MainActivity 和 SplashActivity 從 AppCompatActivity 改為 Activity
- 版本號更新為 3.0.0
- 應用程式標籤更新為 "HeliRFID Ultimate v3"
- 啟動畫面更新為 v3.0 版本品牌識別

### v2.0.0 (2025-03-14) - Ultimate Edition

**新增:**
- NFC 卡片自動讀取
- UID 雙重轉換格式（HEX / 10碼 / 8碼）
- Wiegand 通訊協定轉換（Wiegand26）
- 卡片類型識別（MIFARE Classic / Ultralight / ISO 14443-4）
- 複製風險分析（Clone Analyzer）
- Memory Dump 功能
- CSV 歷史記錄匯出功能
- 50+ 種門禁設備演算法框架

**變更:**
- 完整包名重構為 com.helirfid.ultimate
- 更新至 Material3 設計風格
- 更新 Theme.Material3.DayNight.NoActionBar
- 深色模式支援
- update targetSdk 至 34 (Android 14)

### v1.1.0 (2025-03-14)

**新增:**
- 卡號自動補滿 10 碼（不足時在前面補零）
- 清除歷史紀錄按鈕
- 清除歷史記錄後 Toast 提示

**改進:**
- Material Design 風格優化
- 美化主畫面佈局
- 藍色標題文字 (#1565C0)
- 結果卡片陰影效果
- 清除歷史紀錄按鈕紅色配色

### v1.0.0 (2025-03-14)

**新增:**
- NFC 卡片讀取功能
- UID 轉門禁卡號功能
- 手動輸入 UID 功能
- 歷史記錄管理（100 筆）
- 啟動動畫畫面
- HR 品牌圖示

---

## 📞 聯絡方式

### 聯絡資訊

- **GitHub**: [js0935](https://github.com/js0935)
- **專案**: [HeliRFID](https://github.com/js0935/HeliRFID)
- **問題回報**: [Issues](https://github.com/js0935/HeliRFID/issues)

### 時區

UTC+8 (亞洲/台北)

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

## 🔗 相關連結

- [Android NFC Documentation](https://developer.android.com/guide/topics/connectivity/nfc)
- [Material Design Guidelines](https://material.io/design)
- [Gradle Documentation](https://docs.gradle.org/current/userguide/userguide.html)

---

<div align="center">

**Made with ❤️ by [禾秝軟體開發團隊](https://github.com/js0935)**

[⬆ 回到頂部](#helifid---nfc-門禁卡讀取器)

⭐ 如果喜歡這個專案，請給我們一顆星星！

</div>
