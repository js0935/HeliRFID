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
- UID 多格式轉換（10 碼 / 8 碼 / HEX / Wiegand）
- 歷史記錄管理
- Memory Dump 檢視
- CSV 匯出功能

### 技術棧

| 技術 | 版本 | 說明 |
|------|------|------|
| Java | 8 | 開發語言 |
| Android SDK | 34 (Android 14) | 目標 SDK |
| Min SDK | 23 (Android 6.0) | 最低支援版本 |
| Gradle | 8.0 | 構建工具 |
| AndroidX | 1.11.0 | Android 支援庫 |
| Material Design | 1.11.0 | Material3 支援 |

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
   - 確保已安裝 Android Studio
   - SDK API 23-34 都需要安裝

3. **Gradle**
   ```bash
   # 使用專案內建的 Gradle
   cd heli_rfid_nfc
   ./gradle-8.0/bin/gradle --version
   ```

### IDE 推薦

- **Android Studio** (推薦) - 完整的 Android 開發工具
- IntelliJ IDEA - 也可以用於 Android 開發
- VS Code + Android Extension - 輕量級選擇

### Android Studio 設定

1. 下載並安裝 [Android Studio](https://developer.android.com/studio)
2. 開啟專案：`File` → `Open` → 選擇專案目錄
3. 等待 Gradle 同步完成（可能需要幾分鐘）
4. 執行：點擊 `Run` 按鈕或按 `Shift+F10`

---

## 專案結構

```
heli_rfid_nfc/
├── app/                                          # 應用程式模組
│   ├── src/
│   │   └── main/
│   │       ├── java/com/helirfid/               # Java 原始碼
│   │       │   ├── MainActivity.java            # 主畫面與 NFC 功能
│   │       │   ├── SplashActivity.java          # 啟動畫面
│   │       │   ├── MemoryDumpActivity.java      # 記憶體檢視 Activity
│   │       │   ├── DumpAdapter.java             # RecyclerView 適配器
│   │       │   ├── DumpItem.java                # 扇區/區塊資料模型
│   │       │   ├── Converter.java               # UID 轉換邏輯
│   │       │   ├── HistoryManager.java          # 歷史記錄管理
│   │       │   ├── NFCReader.java               # NFC UID 讀取
│   │       │   ├── Wiegand.java                 # Wiegand26/34 通訊協定
│   │       │   ├── CardAnalyzer.java            # 卡片類型識別
│   │       │   └── CsvExporter.java             # CSV 匯出功能
│   │       ├── res/                             # 資源文件
│   │       │   ├── drawable/
│   │       │   │   └── ic_hr.xml                # HR 品牌圖示
│   │       │   ├── layout/
│   │       │   │   ├── activity_main.xml         # 主畫面佈局
│   │       │   │   ├── activity_memory_dump.xml  # Memory Dump 佈局
│   │       │   │   ├── dump_item.xml            # RecyclerView 項目佈局
│   │       │   │   └── splash.xml               # 啟動畫面佈局
│   │       │   ├── mipmap-anydpi-v26/
│   │       │   │   └── ic_launcher.xml          # Launcher 圖示
│   │       │   ├── values/
│   │       │   │   └── colors.xml              # 顏色設定
│   │       │   └── xml/
│   │       │       └── nfc_tech_filter.xml     # NFC 技術過濾器
│   │       └── AndroidManifest.xml              # 應用程式清單
│   ├── build.gradle                              # 模組構建設置
│   └── proguard-rules.pro                        # ProGuard 規則
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties             # Gradle Wrapper 配置
├── releases/                                      # APK 發布目錄
├── .gitignore                                     # Git 忽略文件
├── build.gradle                                   # 專案構建設置
├── settings.gradle                                # Gradle 設定
├── gradle.properties                              # Gradle 屬性
├── README.md                                      # 專案說明
├── CONTRIBUTING.md                                # 貢獻指南
├── CHANGELOG.md                                   # 變更日誌
├── DEVELOPMENT.md                                 # 本文件
└── LICENSE                                        # MIT 授權文字
```

---

## 核心功能說明

### 1. NFC 讀取功能

**位置**: `MainActivity.java`

```java
@Override
protected void onNewIntent(Intent intent) {
    if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        processTag(tag);
    }
}

private void processTag(Tag tag) {
    String uid = NFCReader.getUID(tag);
    String card10 = Converter.decimal10(tag.getId());
    String card8 = Converter.decimal8(tag.getId());
    String w26 = Wiegand.wiegand26(card10);

    txtUID.setText("UID: " + uid);
    txtCard10.setText("10碼: " + card10);
    txtCard8.setText("8碼: " + card8);
    txtW26.setText("Wiegand26: " + w26);

    historyManager.add(card10);
}
```

**關鍵點**:
- 使用 `NfcAdapter` 處理 NFC Intent
- 在 `onResume()` 中啟用前景派發
- 在 `onPause()` 中停用前景派發
- 支援 `ACTION_TAG_DISCOVERED`、`ACTION_TECH_DISCOVERED`、`ACTION_NDEF_DISCOVERED`

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

public static String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
        sb.append(String.format("%02X ", b));
    }
    return sb.toString().trim();
}

public static String decimal10(byte[] uid) {
    return convert(bytesToHex(uid));
}

public static String decimal8(byte[] uid) {
    String ten = convert(bytesToHex(uid));
    if (ten.length() > 8)
        return ten.substring(ten.length() - 8);
    return ten;
}
```

**轉換步驟**:
1. 清理冒號分隔符
2. 取最後 8 個字元（4 個位元組）
3. 反轉位元組順序（little-endian）
4. 16 進制轉 10 進制
5. 取後 10 碼或 8 碼

### 3. Wiegand 協定轉換

**位置**: `Wiegand.java`

```java
public static String wiegand26(String card10) {
    if (card10.length() != 10) return "格式錯誤";

    long card = Long.parseLong(card10);
    if (card > 65535) {
        return String.format("%04X-%04X-%04X-%04X",
            (int) ((card >> 16) & 0xFFFF),
            (int) (card & 0xFFFF),
            0, 0);
    } else {
        return String.format("0000-0000-%04X-%04X",
            (int) (card & 0xFFFF), 0);
    }
}

public static String wiegand34(String card10) {
    if (card10.length() != 10) return "格式錯誤";

    long card = Long.parseLong(card10);
    return String.format("%04X-%04X-%04X-%04X",
        0, 0, (int) ((card >> 16) & 0xFFFF), (int) (card & 0xFFFF));
}
```

### 4. 歷史記錄管理

**位置**: `HistoryManager.java`

```java
public void add(String card) {
    List<String> list = get();
    list.add(0, card);

    if (list.size() > 100) {
        list.remove(list.size() - 1);
    }

    prefs.edit()
         .putString("data", String.join(",", list))
         .apply();
}

public List<String> get() {
    String data = prefs.getString("data", "");
    if (data.isEmpty()) return new ArrayList<>();

    return new ArrayList<>(Arrays.asList(data.split(",")));
}

public void clear() {
    prefs.edit().clear().apply();
}
```

**特點**:
- 使用 SharedPreferences 儲存
- 最多保存 100 筆記錄
- 最新記錄排在最前面
- 支援一鍵清除

### 5. Memory Dump 功能

**位置**: `MemoryDumpActivity.java`, `DumpAdapter.java`, `DumpItem.java`

```java
// MemoryDumpActivity - RecyclerView 管理
private void readDump() {
    try {
        MifareClassic mifare = MifareClassic.get(tagCopy);
        int sectorCount = mifare.getSectorCount();

        List<DumpItem> dumpList = new ArrayList<>();

        for (int sector = 0; sector < sectorCount; sector++) {
            int blockCount = mifare.getBlockCountInSector(sector);

            for (int block = 0; block < blockCount; block++) {
                String data = readBlock(mifare, sector, block);
                DumpItem item = new DumpItem(
                    String.valueOf(sector),
                    String.valueOf(block),
                    data,
                    block == 0 ? "Sector Trailer" : "Data Block"
                );
                dumpList.add(item);
            }
        }

        DumpAdapter adapter = new DumpAdapter(this, dumpList);
        recyclerView.setAdapter(adapter);

    } catch (Exception e) {
        Toast.makeText(this, "讀取失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
```

**DumpItem 資料模型**:
```java
public class DumpItem {
    private String sector;
    private String block;
    private String data;
    private String description;

    // 建構函式、getter、setter
}
```

**DumpAdapter RecyclerView 適配器**: 繫結資料到 RecyclerView 視圖

### 6. CSV 匯出

**位置**: `CsvExporter.java`

```java
public static boolean export(Context context, List<String> history) {
    try {
        String fileName = "HeliRFID_History.csv";
        String csvContent = generateContent(history);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return exportWithMediaStore(context, fileName, csvContent);
        } else {
            return exportLegacy(context, fileName, csvContent);
        }
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}

private static boolean exportWithMediaStore(Context context, String fileName, String csvContent) {
    ContentResolver resolver = context.getContentResolver();
    ContentValues values = new ContentValues();

    values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
    values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
    values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

    android.net.Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
    if (uri == null) return false;

    try (OutputStream outputStream = resolver.openOutputStream(uri);
         BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
        writer.write(csvContent);
        writer.flush();
        return true;
    }
}

private static boolean exportLegacy(Context context, String fileName, String csvContent) {
    File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    File file = new File(path, fileName);

    if (!path.exists()) path.mkdirs();

    try (FileWriter fw = new FileWriter(file);
         BufferedWriter bw = new BufferedWriter(fw)) {
        bw.write(csvContent);
        bw.flush();
        return true;
    }
}
```

**支援兩種方法**:
- Android 10+ (API 29+)：使用 MediaStore API
- Android 9 以下：使用傳統 File IO

### 7. NFC Reader

**位置**: `NFCReader.java`

```java
public static String getUID(Tag tag) {
    byte[] uid = tag.getId();
    return Converter.bytesToHex(uid);
}

public static void setLastTag(Tag tag) {
    lastTag = tag;
}

public static Tag getLastTag() {
    return lastTag;
}
```

### 8. Card Analyzer

**位置**: `CardAnalyzer.java`

```java
public static String analyze(Tag tag) {
    StringBuilder result = new StringBuilder();

    String[] techList = tag.getTechList();
    for (String tech : techList) {
        if (tech.contains("MifareClassic")) {
            result.append("MIFARE Classic\n");
        } else if (tech.contains("MifareUltralight")) {
            result.append("MIFARE Ultralight\n");
        } else if (tech.contains("IsoDep")) {
            result.append("ISO 14443 Type A/B\n");
        } else if (tech.contains("NfcA") || tech.contains("NfcB")) {
            result.append("ISO 14443\n");
        }
    }

    if (result.length() == 0) {
        result.append("未知卡片類型");
    }

    return result.toString();
}
```

---

## 構建與測試

### 構建 Debug 版本

```bash
cd heli_rfid_nfc

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

# 啟動應用程式
adb shell am start -n com.helirfid/.MainActivity
```

---

## 部署流程

### 準備發布

#### 1. 更新版本號

```gradle
// app/build.gradle
android {
    defaultConfig {
        applicationId "com.helirfid"
        versionCode 8
        versionName "4.0.2"
    }
}
```

#### 2. 更新文件

- 更新 `CHANGELOG.md`：添加新版本變更
- 更新 `README.md`：更新版本資訊和下載連結
- 更新啟動畫面版本號

#### 3. 更新 AndroidManifest

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.helirfid">

    <application
        android:icon="@mipmap/ic_launcher"
        android:roundIcon="@mipmap/ic_launcher"
        android:label="HeliRFID 專業版"
        android:requestLegacyExternalStorage="true"
        android:theme="@style/Theme.Material3.DayNight.NoActionBar">
        ...
    </application>
</manifest>
```

#### 4. 構建 APK

```bash
./gradle-8.0/bin/gradle clean assembleDebug
```

#### 5. 複製 APK 到 releases 目錄

```bash
cp app/build/outputs/apk/debug/app-debug.apk releases/HeliRFIDProfessional-v4.0.2-debug.apk
```

#### 6. 提交變更

```bash
git add .
git commit -m "release: HeliRFID v4.0.2

- 更新版本號為 4.0.2
- 新增功能 X
- 修復 bug Y"
git push origin master
```

#### 7. 創建 Git Tag

```bash
git tag -a v4.0.2 -m "HeliRFID Professional v4.0.2 - Release"
git push origin v4.0.2
```

#### 8. 創建 GitHub Release

1. 前往 [Releases 頁面](https://github.com/js0935/HeliRFID/releases)
2. 點擊「Create a new release」
3. 填寫資訊：
   - **Tag**: `v4.0.2`
   - **Title**: `HeliRFID Professional v4.0.2`
   - **Description**: 從 CHANGELOG.md 複製新增和修復內容
4. 上傳 APK 檔案
5. 點擊「Publish release」

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
A: 在 Android Studio 中配置 SDK 路徑：
- File → Settings → Appearance & Behavior → System Settings → Android SDK
- 設置 Android SDK Location

**Q: 編譯錯誤：找不到類別**
A: 清理並重建：
```bash
./gradle-8.0/bin/gradle clean build
```

**Q: 警告：compileSdk = 34 測試只到 33**
A: 這是正常的警告，可以安全忽略或添加：
```gradle
android {
    compileSdk 34
    ...
}

# gradle.properties
android.suppressUnsupportedCompileSdk=34
```

### NFC 問題

**Q: NFC 功能不工作**
A: 檢查：
- 裝置是否支援 NFC
- NFC 是否已啟用（設定 → NFC）
- 權限是否已授予（`<uses-permission android:name="android.permission.NFC"/>`）
- NFC 技術過濾器配置是否正確

**Q: 在模擬器上測試 NFC**
A: 模擬器不支援 NFC，需要使用實體支援 NFC 的裝置

**Q: NFC Intent 不觸發**
A: 檢查：
1. `AndroidManifest.xml` 中的 Intent Filter 配置
2. `onResume()` 中是否啟用了前景派發
3. `NfcAdapter` 是否為 null

### 實際裝置問題

**Q: APK 安裝失敗**
A: 檢查：
- 是否允許「來源不明應用程式」安裝
- 安裝的 APK 版本是否高於已安裝版本
- 手機磁碟空間是否足夠

**Q: 應用程式崩潰**
A: 查看 Logcat：
```bash
adb logcat -s HeliRFID:V AndroidRuntime:E
```

或使用 Android Studio 的 Logcat 視圖

**Q: CSV 匯出失敗（Android 10+）**
A: 檢查：
- 是否授予儲存空間權限
- MediaStore API 使用是否正確
- Downloads 目錄是否可寫入

---

## 有用的命令

### ADB 命令

```bash
# 查看連接的裝置
adb devices

# 安裝 APK
adb install -r app-debug.apk

# 卸載應用程式
adb uninstall com.helirfid

# 查看 Logcat
adb logcat

# 清除應用程式資料
adb shell pm clear com.helirfid

# 啟動應用程式
adb shell am start -n com.helirfid/.MainActivity

# 重啟應用程式
adb shell am force-stop com.helirfid && adb shell am start -n com.helirfid/.MainActivity
```

### Gradle 命令

```bash
# 查看所有可用的任務
./gradle-8.0/bin/gradle tasks

# 查看依賴樹
./gradle-8.0/bin/gradle :app:dependencies

# 生成報告
./gradle-8.0/bin/gradle assembleDebug --info

# 查看版本
./gradle-8.0/bin/gradle --version
```

### Git 命令

```bash
# 查看待提交變更
git status

# 提交變更
git add .
git commit -m "commit message"

# 推送到遠端
git push origin master

# 拉取最新變更
git pull origin master

# 查看提交歷史
git log --oneline
```

---

## 參考資源

### Android 開發

- [Android Developers](https://developer.android.com/)
- [Android NFC Guide](https://developer.android.com/guide/topics/connectivity/nfc)
- [Material Design Guidelines](https://material.io/design)
- [MifareClassic 官方文檔](https://www.nxp.com/docs/en/data-sheet/MF1S503x.pdf)

### 工具

- [Gradle Documentation](https://docs.gradle.org/current/userguide/userguide.html)
- [Git Documentation](https://git-scm.com/doc)
- [ADB Documentation](https://developer.android.com/studio/command-line/adb)

### 社群

- [Stack Overflow - Android](https://stackoverflow.com/questions/tagged/android)
- [HeliRFID GitHub](https://github.com/js0935/HeliRFID)
- [HeliRFID Issues](https://github.com/js0935/HeliRFID/issues)

---

## 代碼規範

### Java 樣式

遵循 [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)

#### 命名慣例

| 類型 | 慣例 | 範例 |
|------|------|------|
| 類別 | PascalCase | `MainActivity` |
| 方法 | camelCase | `convertUid()` |
| 變數 | camelCase | `cardNumber` |
| 常量 | UPPER_SNAKE_CASE | `MAX_HISTORY_SIZE` |
| 套件 | lowercase | `com.helirfid` |

#### 程式碼示例

```java
// 好的做法
public class Converter {

    public static String convert(String uid) {
        if (uid == null || uid.isEmpty()) {
            return "錯誤";
        }

        uid = uid.replace(":", "");

        if (uid.length() < 8) {
            return "錯誤";
        }

        String last4 = uid.substring(uid.length() - 8);
        String reversed = reverseBytes(last4);

        BigInteger dec = new BigInteger(reversed, 16);
        String num = dec.toString();

        if (num.length() > 10) {
            num = num.substring(num.length() - 10);
        }

        return num;
    }

    private static String reverseBytes(String hex) {
        // 實現
        return "";
    }
}
```

### XML 樣式

```xml
<!-- 好的做法 -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical">

    <TextView
        android:id="@+id/result"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Result" />

</LinearLayout>
```

---

## 聯絡方式

如有開發相關問題，請：

- 開啟 [GitHub Issue](https://github.com/js0935/HeliRFID/issues)
- 聯絡: [js0935](https://github.com/js0935)

---

Happy Coding! 🎉
