# 開發者指南 (Development Guide)

本文件提供 HeliRFID 專案的詳細開發資訊。

---

## 📚 目錄

- [專案概述](#專案概述)
- [開發環境設定](#開發環境設定)
- [專案結構](#專案結構)
- [核心功能說明](#核心功能說明)
- [構建與測試](#構建與測試)
- [部署流程](#部署流程)
- [常見問題](#常見問題)

---

## 專案概述

### 目標

開發一款簡單易用的 Android NFC 門禁卡讀取應用程式，支援：

- NFC 卡片自動讀取
- UID 轉門禁卡號
- 歷史記錄管理
- 美觀的使用者介面

### 技術棧

| 技術 | 版本 | 說明 |
|------|------|------|
| Java | 8 | 開發語言 |
| Android SDK | 33 (Android 13) | 目標 SDK |
| Min SDK | 24 (Android 7.0) | 最低支援版本 |
| Gradle | 8.0 | 構建工具 |
| AndroidX | 1.6.1 | Android 支援庫 |

---

## 開發環境設定

### 必要工具

1. **Java Development Kit (JDK)**
   ```bash
   # 檢查 Java 版本
   java -version
   # 需要 JDK 8 或更高版本
   ```

2. **Android SDK**
   ```bash
   # 檢查 SDK 路徑
   echo $ANDROID_HOME
   ```

3. **Gradle**
   ```bash
   # 使用專案內建的 Gradle
   cd HeliRFID
   ./gradle-8.0/bin/gradle --version
   ```

### IDE 推薦

- Android Studio (推薦)
- IntelliJ IDEA
- VS Code + Android Extension

### Android Studio 設定

1. 下載並安裝 [Android Studio](https://developer.android.com/studio)
2. 開啟專案：File → Open → 選擇專案目錄
3. 等待 Gradle 同步完成
4. 執行 Run 按鈕或 Shift+F10

---

## 專案結構

```
HeliRFID/
├── app/                           # 應用程式模組
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/helirfid/app/
│   │   │   │   ├── MainActivity.java           # 主活動
│   │   │   │   ├── SplashActivity.java         # 啟動畫面
│   │   │   │   ├── Converter.java              # UID 轉換器
│   │   │   │   └── HistoryManager.java         # 歷史記錄管理器
│   │   │   ├── res/                            # 資源文件
│   │   │   │   ├── drawable/
│   │   │   │   │   └── ic_hr.xml              # HR 圖示
│   │   │   │   ├── layout/
│   │   │   │   │   ├── activity_main.xml      # 主畫面佈局
│   │   │   │   │   └── splash.xml             # 啟動畫面佈局
│   │   │   │   ├── mipmap-anydpi-v26/
│   │   │   │   │   └── ic_launcher.xml        # Launcher 圖示
│   │   │   │   └── values/
│   │   │   │       └── colors.xml             # 顏色資源
│   │   │   └── AndroidManifest.xml            # 應用程式清單
│   │   ├── test/                              # 單元測試
│   │   └── androidTest/                       # UI 測試
│   ├── build.gradle                           # 模組構建設置
│   └── proguard-rules.pro                     # ProGuard 規則
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties          # Gradle Wrapper 配置
├── .gitignore                                 # Git 忽略文件
├── build.gradle                               # 專案構建設置
├── gradle.properties                         # Gradle 屬性
├── settings.gradle                           # Gradle 設定
├── README.md                                  # 專案說明
├── CONTRIBUTING.md                            # 貢獻指南
├── CHANGELOG.md                               # 變更日誌
├── LICENSE                                    # 授權文件
└── DEVELOPMENT.md                             # 本文件
```

---

## 核心功能說明

### 1. NFC 讀取功能

**位置**: `MainActivity.java`

```java
@Override
protected void onNewIntent(Intent intent) {
    Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
    byte[] uid = tag.getId();
    String hex = Converter.bytesToHex(uid);
    String card = Converter.convert(hex);
    // 顯示結果
}
```

**關鍵點**:
- 使用 `NfcAdapter` 處理 NFC Intent
- 在 `onResume()` 中啟用前景派發
- 在 `onPause()` 中停用前景派發

### 2. UID 轉換邏輯

**位置**: `Converter.java`

```java
public static String convert(String uid) {
    uid = uid.replace(":", "");
    if (uid.length() < 8) return "錯誤";

    String last4 = uid.substring(uid.length() - 8);
    String reversed = last4.substring(6, 8) +
                      last4.substring(4, 6) +
                      last4.substring(2, 4) +
                      last4.substring(0, 2);

    BigInteger dec = new BigInteger(reversed, 16);
    String num = dec.toString();

    if (num.length() > 10)
        num = num.substring(num.length() - 10);

    return num;
}
```

**轉換步驟**:
1. 清理冒號分隔符
2. 取最後 8 個字元
3. 反轉位元組順序
4. 16 進制轉 10 進制
5. 取後 10 碼

### 3. 歷史記錄管理

**位置**: `HistoryManager.java`

```java
public void add(String card) {
    List<String> list = get();
    list.add(0, card); // 添加到開頭
    if (list.size() > 100)
        list.remove(list.size() - 1); // 清理舊記錄

    prefs.edit()
         .putString("data", String.join(",", list))
         .apply();
}
```

**特點**:
- 使用 SharedPreferences 儲存
- 最多保存 100 筆記錄
- 最新記錄排在最前面

---

## 構建與測試

### 構建 Debug 版本

```bash
# 編譯 debug APK
./gradle-8.0/bin/gradle assembleDebug

# 輸出路徑
app/build/outputs/apk/debug/app-debug.apk
```

### 構建 Release 版本

```bash
# 編譯 release APK
./gradle-8.0/bin/gradle assembleRelease

# 輸出路徑
app/build/outputs/apk/release/app-release-unsigned.apk
```

### 清理構建

```bash
# 清理所有構建檔案
./gradle-8.0/bin/gradle clean
```

### 測試

```bash
# 執行單元測試
./gradle-8.0/bin/gradle test

# 執行 UI 測試
./gradle-8.0/bin/gradle connectedAndroidTest
```

### APK 安裝

```bash
# 使用 ADB 安裝
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 部署流程

### 準備發布

1. **更新版本號**
   ```gradle
   // build.gradle
   defaultConfig {
       versionCode 2
       versionName "1.0.1"
   }
   ```

2. **更新 CHANGELOG.md**
   ```markdown
   ## [1.0.1] - 2025-03-xx
   - 新增功能 X
   - 修復 bug Y
   ```

3. **簽署 APK**

   創建簽名配置：
   ```bash
   keytool -genkey -v -keystore heli-rfid.keystore \
     -alias helirfid -keyalg RSA -keysize 2048 \
     -validity 10000
   ```

   配置 build.gradle：
   ```gradle
   signingConfigs {
       release {
           storeFile file("heli-rfid.keystore")
           storePassword "your-password"
           keyAlias "helirfid"
           keyPassword "your-password"
       }
   }

   buildTypes {
       release {
           signingConfig signingConfigs.release
           minifyEnabled true
           proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'),
                        'proguard-rules.pro'
       }
   }
   ```

4. **構建 Release APK**
   ```bash
   ./gradle-8.0/bin/gradle assembleRelease
   ```

5. **推送 APK 到 GitHub**
   ```bash
   cp app/build/outputs/apk/release/app-release.apk releases/HeliRFID-v1.0.1.apk
   git add releases/
   git commit -m "Release v1.0.1"
   git push origin master
   ```

### 創建 GitHub Release

1. 前往 [Releases 頁面](https://github.com/js0935/HeliRFID/releases)
2. 點擊「Create a new release」
3. 填寫資訊：
   - Tag: `v1.0.1`
   - Title: `HeliRFID v1.0.1`
   - Description: 複製 CHANGELOG 內容
   - Assets: 上傳 APK 檔案
4. 點擊「Publish release」

---

## 常見問題

### 構建問題

**Q: Gradle 同步失敗**
A: 嘗試以下步驟：
```bash
./gradle-8.0/bin/gradle clean
./gradle-8.0/bin/gradle --refresh-dependencies
```

**Q: 找不到 Android SDK**
A: 設定 ANDROID_HOME 環境變數：
```bash
export ANDROID_HOME=/path/to/android/sdk
```

**Q: 編譯錯誤：找不到類別**
A: 清理並重建：
```bash
./gradle-8.0/bin/gradle clean build
```

### NFC 問題

**Q: NFC 功能不工作**
A: 檢查：
- 裝置是否支援 NFC
- NFC 是否已啟用
- 權限是否已授予
- 程式碼中是否正確配置了 Intent Filter

**Q: 在模擬器上測試 NFC**
A: 需要使用實體裝置，模擬器不支援 NFC

### 實際裝置問題

**Q: APK 安裝失敗**
A: 檢查：
- 允許未知來源安裝
- 確認 APK 簽名
- 檢查磁碟空間

**Q: 應用程式崩潰**
A: 查看 Logcat：
```bash
adb logcat -s System.err:I HeliRFID:V
```

---

## 有用的命令

### ADB 命令

```bash
# 查看連接的裝置
adb devices

# 安裝 APK
adb install -r app-debug.apk

# 卸載應用程式
adb uninstall com.helirfid.app

# 查看 Logcat
adb logcat

# 清除應用程式資料
adb shell pm clear com.helirfid.app

# 啟動應用程式
adb shell am start -n com.helirfid.app/.MainActivity
```

### Gradle 命令

```bash
# 查看所有可用的任務
./gradle-8.0/bin/gradle tasks

# 查看依賴樹
./gradle-8.0/bin/gradle :app:dependencies

# 生成報告
./gradle-8.0/bin/gradle assembleDebug --info
```

### Git 命令

```bash
# 查看變更
git status

# 提交變更
git add .
git commit -m "message"

# 推送到遠端
git push origin master

# 拉取最新變更
git pull origin master
```

---

## 參考資源

### Android 開發

- [Android Developers](https://developer.android.com/)
- [Android NFC Guide](https://developer.android.com/guide/topics/connectivity/nfc)
- [Material Design Guidelines](https://material.io/design)

### 工具

- [Gradle Documentation](https://docs.gradle.org/current/userguide/userguide.html)
- [Git Documentation](https://git-scm.com/doc)
- [ADB Documentation](https://developer.android.com/studio/command-line/adb)

### 社群

- [Stack Overflow](https://stackoverflow.com/questions/tagged/android)
- [HeliRFID GitHub](https://github.com/js0935/HeliRFID)
- [HeliRFID Issues](https://github.com/js0935/HeliRFID/issues)

---

## 聯絡方式

如有開發相關問題，請：

- 開啟 [GitHub Issue](https://github.com/js0935/HeliRFID/issues)
- 聯絡: [js0935](https://github.com/js0935)

---

Happy Coding! 🎉
