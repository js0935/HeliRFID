# HeliRFID - NFC 門禁卡讀取器

<div align="center">

![HR Icon](app/src/main/res/drawable/ic_hr.xml)

**禾秝軟體開發團隊**

一款 Android NFC 門禁卡讀取與轉換應用程式

[![Version](https://img.shields.io/badge/version-v1.1.0-blue.svg)](releases/HeliRFID-v1.1-debug.apk)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)
[![Android](https://img.shields.io/badge/android-7.0%2B-green.svg)](https://developer.android.com/)
[![Build](https://img.shields.io/badge/build-passing-brightgreen.svg)](https://github.com/js0935/HeliRFID)

</div>

---

## 📋 功能特點

### 核心功能
- ✅ **NFC 卡片讀取** - 自動讀取 NFC 卡片 UID
- ✅ **UID 轉換** - 將 UID 轉換為門禁卡號（固定10碼，不足補零）
- ✅ **手動輸入** - 支援手動輸入 UID 進行轉換
- ✅ **歷史記錄** - 自動儲存最近 100 筆卡號記錄
- ✅ **清除記錄** - 一鍵清除所有歷史記錄

### 使用者介面
- ✅ **美觀圖示** - HR 品牌藍色圖示
- ✅ **啟動動畫** - 2 秒 Splash 畫面
- ✅ **Material Design** - 現代化設計風格
- ✅ **簡潔設計** - 清晰的操作介面
- ✅ **品牌標示** - 禾秝軟體開發團隊品牌形象
- ✅ **美化介面** - 陰影效果、圓角設計

---

## 🔧 系統需求

| 項目 | 需求 |
|------|------|
| 作業系統 | Android 7.0 (API 24) 或更高版本 |
| NFC | 必須支援 NFC 功能 |
| 儲存空間 | 約 5 MB |
| 權限 | NFC 權限 |

---

## 📱 下載 APK

### 最新版本 (v1.1.0)

**[📥 下載 HeliRFID-v1.1-debug.apk](releases/HeliRFID-v1.1-debug.apk)** (2.9 MB)

**版本日期**: 2025-03-14

### 版本資訊
- 版本號: 1.1.0 (2)
- 類型: Debug
- 構建: Gradle 8.0
- 最低 SDK: API 24
- 目標 SDK: API 33

### v1.1.0 新增功能
- ✨ 卡號自動補滿 10 碼
- ✨ 清除歷史紀錄功能
- ✨ Material Design 風格優化
- ✨ 美化使用者介面

**舊版本:**
- [v1.0.0](releases/HeliRFID-v1.0-debug.apk) (2025-03-14)

---

## 🚀 安裝方式

### 方法 1: 直接安裝 APK (推薦)

1. 下載 [HeliRFID-v1.0-debug.apk](releases/HeliRFID-v1.0-debug.apk)
2. 在手機上開啟 APK 檔案
3. 允許「來源不明應用」安裝權限
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

### NFC 讀取

**步驟:**
1. 確保手機 NFC 功能已開啟（設定 → NFC）
2. 開啟 HeliRFID 應用程式
3. 將 NFC 卡片靠近手機背面 NFC 感應區
4. 應用程式會自動讀取並顯示：
   - UID 原始值（16進制格式，如：04:A2:B3:C4:D5:E6:78）
   - 轉換後的門禁卡號（後10碼）

**注意:**
- 保持卡片與手機距離在 1-3 公分內
- 掃描時不要快速移動卡片
- 某些卡片可能需要特定的位置才能正確讀取

### 手動輸入

**步驟:**
1. 在主畫面的「手動輸入 UID」欄位輸入 UID
2. 支援的格式：
   - 帶冒號：04:A2:B3:C4:D5:E6:78
   - 不帶冒號：04A2B3C4D5E678
3. 點擊「轉換」按鈕
4. 查看轉換結果

**提示:**
- UID 長度建議至少 8 個字元
- 錯誤輸入會顯示「錯誤」

### 歷史記錄

**功能說明:**
- 應用程式自動儲存最近 100 筆卡號
- 最新掃描記錄顯示在最上方
- 在「卡號歷史」區域查看所有記錄
- 應用程式關閉後記錄仍會保留

**清除記錄:**
目前版本不支援手動清除記錄，如需清除請：
1. 前往應用程式設定
2. 清除應用程式資料
3. 重新開啟應用程式

---

## 🛠 技術架構

### 開發工具

| 技術 | 版本 | 說明 |
|------|------|------|
| 開發語言 | Java 8 | 程式碼語言 |
| 構建工具 | Gradle 8.0 | 自動化構建 |
| 最低 SDK | API 24 (Android 7.0) | 支援的最低版本 |
| 目標 SDK | API 33 (Android 13) | 主要目標版本 |
| AndroidX | 1.6.1 | Android 支援庫 |

### 專案結構

```
HeliRFID/
├── app/                          # 應用程式模組
│   ├── src/
│   │   └── main/
│   │       ├── java/com/helirfid/app/
│   │       │   ├── MainActivity.java      # 主畫面與 NFC 功能
│   │       │   ├── SplashActivity.java    # 啟動畫面
│   │       │   ├── Converter.java         # UID 轉換邏輯
│   │       │   └── HistoryManager.java    # 歷史記錄管理
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
- 手動輸入處理
- 歷史記錄顯示

#### 2. Converter
- UID 格式清理
- 16 進制轉 10 進制
- 位元組反轉
- 取後 10 碼

#### 3. HistoryManager
- SharedPreferences 管理
- 記錄儲存與檢索
- 最多 100 筆限制
- 最新記錄排序

#### 4. SplashActivity
- 啟動動畫顯示
- 2 秒延遲
- 自動跳轉主畫面

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

**Q: 支援哪些 NFC 卡片？**
A: 支援大部分標準 13.56 MHz NFC 卡片，包括：
- MIFARE Classic (1k, 4k)
- MIFARE Ultralight
- ISO 14443 Type A/B 卡片
- FeliCa 卡片

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
A: 目前版本不支援手動清除，可以：
1. 前往應用程式設定
2. 清除應用程式資料
3. 重新開啟應用程式

### 技術問題

**Q: 在模擬器上運行？**
A: 不支援，因為模擬器不提供 NFC 硬體功能。

**Q: APK 簽名資訊？**
A: 這是 Debug 版本，使用 Debug 簽金。
   如需正式版本，請使用 Release 簽金重新編譯。

**Q: 程式碼可以商業使用嗎？**
A: 可以，本專案採用 MIT 授權，可自由使用和修改。

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

查看最新變更：[CHANGELOG.md](CHANGELOG.md)

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
