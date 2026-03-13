# HeliRFID - NFC 門禁卡讀取器

<div align="center">

![HR Icon](app/src/main/res/drawable/ic_hr.xml)

**禾秝軟體開發團隊**

一款 Android NFC 門禁卡讀取與轉換應用程式

</div>

---

## 📋 功能特點

- ✅ **NFC 卡片讀取** - 自動讀取 NFC 卡片 UID
- ✅ **UID 轉換** - 將 UID 轉換為門禁卡號（後10碼）
- ✅ **手動輸入** - 支援手動輸入 UID 進行轉換
- ✅ **歷史記錄** - 自動儲存最近 100 筆卡號記錄
- ✅ **美觀圖示** - HR 品牌藍色圖示
- ✅ **啟動動畫** - 2 秒 Splash 畫面

---

## 🔧 系統需求

- Android 7.0 (API 24) 或更高版本
- NFC 功能支援

---

## 📱 下載 APK

**最新版本 APK:**

- [app-debug.apk](app/build/outputs/apk/debug/app-debug.apk) (2.9 MB)

---

## 🚀 安裝方式

### 方法 1: 直接安裝 APK

1. 下載 [app-debug.apk](app/build/outputs/apk/debug/app-debug.apk)
2. 在手機上開啟 APK 檔案並安裝
3. 允許「來源不明應用」安裝權限

### 方法 2: 從原始碼編譯

```bash
# 克隆專案
git clone https://github.com/js0935/HeliRFID.git

# 進入專案目錄
cd HeliRFID

# 編譯 APK
./gradle-8.0/bin/gradle assembleDebug

# APK 輸出路徑
app/build/outputs/apk/debug/app-debug.apk
```

---

## 📖 使用說明

### NFC 讀取

1. 確保手機 NFC 功能已開啟
2. 開啟 HeliRFID 應用程式
3. 將 NFC 卡片靠近手機背面
4. 應用程式會自動讀取並顯示：
   - UID 原始值（16進制）
   - 轉換後的門禁卡號（後10碼）

### 手動輸入

1. 在主畫面的「手動輸入 UID」欄位輸入 UID
2. 點擊「轉換」按鈕
3. 查看轉換結果

### 歷史記錄

- 應用程式自動儲存最近 100 筆卡號
- 在「卡號歷史」區域查看掃描記錄

---

## 🛠 技術架構

### 開發工具

- **語言**: Java
- **構建工具**: Gradle 8.0
- **最低 SDK**: API 24 (Android 7.0)
- **目標 SDK**: API 33 (Android 13)
- **依賴庫**:
  - `androidx.appcompat:appcompat:1.6.1`

### 專案結構

```
HeliRFID/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/helirfid/app/
│   │       │   ├── MainActivity.java      # 主畫面與 NFC 功能
│   │       │   ├── SplashActivity.java    # 啟動畫面
│   │       │   ├── Converter.java         # UID 轉換邏輯
│   │       │   └── HistoryManager.java    # 歷史記錄管理
│   │       ├── res/
│   │       │   ├── drawable/
│   │       │   │   └── ic_hr.xml          # HR 圖示
│   │       │   ├── layout/
│   │       │   │   ├── activity_main.xml  # 主畫面佈局
│   │       │   │   └── splash.xml         # 啟動畫面佈局
│   │       │   ├── mipmap-anydpi-v26/
│   │       │   │   └── ic_launcher.xml    # Launcher Icon
│   │       │   └── values/
│   │       │       └── colors.xml         # 顏色設定
│   │       └── AndroidManifest.xml
│   ├── build.gradle
│   └── proguard-rules.pro
├── build.gradle
├── settings.gradle
└── gradle.properties
```

---

## 🔐 權限說明

此應用程式需要以下權限：

- `android.permission.NFC` - 讀取 NFC 卡片

---

## 📝 UID 轉換邏換邏輯

轉換步驟：

1. 移除 UID 中的冒號分隔符
2. 取最後 8 個字元
3. 反轉位元組順序
4. 轉換為 10 進制
5. 取後 10 碼

範例：

```
原始 UID: 04:A2:B3:C4:D5:E6:78
移除冒號: 04A2B3C4D5E678
取後 8 碼: C4D5E678
反轉位元組: 78E6D5C4
轉換 10 進制: 2029985476
後 10 碼: 2029985476
```

---

## 🎨 設計規格

### 品牌色

- **主色**: #1565C0 (藍色)
- **背景色**: #1565C0
- **文字色**: #FFFFFF (白色)

### 圖示設計

- **類型**: Vector Drawable
- **設計**: 適配圖示 (Adaptive Icon)
- **顯示**: HR 白色文字在藍色背景上

---

## 🤝 貢獻

歡迎提交 Issue 和 Pull Request！

---

## 📄 授權

版權所有 © 2025 禾秝軟體開發團隊

---

## 📧 聯絡方式

- **GitHub**: [js0935](https://github.com/js0935)
- **專案**: [HeliRFID](https://github.com/js0935/HeliRFID)

---

<div align="center">

Made with ❤️ by [禾秝軟體開發團隊](https://github.com/js0935)

</div>
