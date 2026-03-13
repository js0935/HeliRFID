# 貢獻指南 (Contributing Guide)

感謝您對 HeliRFID 專案的興趣！我們熱烈歡迎所有形式的貢獻。

---

## 🤝 如何貢獻

### 報告問題 (Bug Report)

如果您發現 bug 或有功能建議，請：

1.前往 [Issues 頁面](https://github.com/js0935/HeliRFID/issues)
2. 點擊「New Issue」
3. 選擇適當的模板：
   - **Bug Report** - 報告 bug
   - **Feature Request** - 新功能建議
4. 填寫詳細資訊：
   - **標題**: 簡潔描述問題
   - **描述**: 詳細說明問題或建議
   - **重現步驟**: Bug 的重現步驟（如果是 bug）
   - **預期行為**: 期望發生什麼
   - **實際行為**: 實際發生什麼
   - **環境資訊**:
     - 裝置型號
     - Android 版本
     - 應用程式版本

### 提交程式碼 (Pull Request)

我們歡迎您提交程式碼改進！

#### 開發流程

1. **Fork 專案**
   ```bash
   # 在 GitHub 上點擊 Fork 按鈕
   ```

2. **克隆您的 Fork**
   ```bash
   git clone https://github.com/YOUR_USERNAME/HeliRFID.git
   cd HeliRFID
   ```

3. **創建分支**
   ```bash
   git checkout -b feature/your-feature-name
   # 或
   git checkout -b fix/your-bug-fix
   ```

4. **進行改進**

   遵循以下變更：
   - 遵守 Google Java 樣式規範
   - 添加適當的註釋
   - 確保程式碼可編譯並通過測試
   - 更新相關的說明文件

5. **測試您的變更**
   ```bash
   # 編譯 debug 版本
   ./gradle-8.0/bin/gradle assembleDebug

   # 編譯 release 版本
   ./gradle-8.0/bin/gradle assembleRelease
   ```

6. **提交變更**
   ```bash
   git add .
   git commit -m "feat: add your feature description"
   ```

   **提交訊息格式:**
   - `feat:` - 新功能
   - `fix:` - Bug 修復
   - `docs:` - 文件更新
   - `style:` - 程式碼樣式（不影響功能）
   - `refactor:` - 重構
   - `perf:` - 效能優化
   - `test:` - 測試相關
   - `chore:` - 構建或工具相關

7. **推送到您的 Fork**
   ```bash
   git push origin feature/your-feature-name
   ```

8. **創建 Pull Request**
   - 前往 https://github.com/js0935/HeliRFID
   - 點擊「New Pull Request」
   - 選擇您的分支
   - 填寫 PR 描述：
     - 清晰說明變更內容
     - 關聯相關的 Issue（如果有）
     - 添加截圖（如果是 UI 變更）

---

## 📋 程式碼規範

### Java 程式碼樣式

遵循 [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)

#### 範例：

```java
// 好的做法
public class Converter {

    public static String convert(String uid) {
        // 實現
    }
}

// 不好的做法
public class converter {
    public static String Convert(String UID) {
        // 實現
    }
}
```

### 命名慣例

| 類型 | 慣例 | 範例 |
|------|------|------|
| 類別 | PascalCase | `MainActivity` |
| 方法 | camelCase | `convertUid()` |
| 變數 | camelCase | `cardNumber` |
| 常量 | UPPER_SNAKE_CASE | `MAX_HISTORY_SIZE` |
| 套件 | lowercase | `com.helirfid.app` |

### XML 程式碼樣式

```xml
<!-- 好的做法 -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content">

    <TextView
        android:id="@+id/result"
        android:text="Result" />

</LinearLayout>
```

### 註釋

```java
/**
 * 將 UID 轉換為門禁卡號
 *
 * @param uid 原始 UID 字串
 * @return 轉換後的門禁卡號（後 10 碼）
 */
public static String convert(String uid) {
    // 實現
}
```

---

## 🎨 設計規範

### 顏色

- **主色**: #1565C0 (藍色)
- **文字色**: #FFFFFF (白色)

### 圖示

- 使用 Vector Drawable 格式
- 遵循 Material Design 指南
- 保持簡潔清晰

---

## 🧪 測試

### 手動測試清單

在提交 PR 前，請確保：

- [ ] 應用程式可以正常安裝
- [ ] NFC 讀取功能正常
- [ ] UID 轉換結果正確
- [ ] 手動輸入功能正常
- [ ] 歷史記錄可以儲存和檢索
- [ ] UI 在不同螢幕尺寸上正常顯示
- [ ] 沒有崩潰或明顯的 bug

### 測試裝置

儘可能在多種裝置上測試：

- 不同 Android 版本（7.0, 8.0, 9.0, 10.0+, 11.0+, 12.0+, 13.0+）
- 不同螢幕尺寸
- 有/無 NFC 功能的裝置

---

## 📝 文件

### 更新說明文件

如果您修改了功能，請更新相關的文件：

- README.md - 用戶文檔
- DEVELOPMENT.md - 開發者文檔
- CHANGELOG.md - 版本更新日誌
- 程式碼內的註釋

### Markdown 格式

```markdown
# 一級標題
## 二級標題
### 三級標題

**粗體文字**
*斜體文字*

[連結文字](https://example.com)

- 無序列表項
- 無序列表項

1. 有序列表項
2. 有序列表項

`程式碼`

```java
// 程式碼區塊
public class Example {
}
```
```

---

## 🐛 調試技巧

### Logcat

使用 Android Studio 的 Logcat 查看日誌：

```java
// 在程式碼中添加日誌
Log.d("HeliRFID", "Debug information");
Log.e("HeliRFID", "Error message", exception);
```

### 常見問題

**問題: NFC 功能不工作**
- 檢查 NFC 是否已啟用
- 檢查權限是否已授予
- 查看裝置是否支援 NFC

**問題: UID 轉換結果不正確**
- 檢查轉換邏輯
- 驗證輸入格式

---

## 💡 建議的功能

我們歡迎新功能建議！一些想法：

- [ ] 支援更多 NFC 卡片類型
- [ ] 添加卡號複製功能
- [ ] 支援匯出/匯入歷史記錄
- [ ] 添加深色模式
- [ ] 多語言支援
- [ ] 卡號備註功能
- [ ] 卡片分類管理
- [ ] 掃描結果分享

---

## 📞 聯絡方式

如有任何問題，請：

- 開啟 [GitHub Issue](https://github.com/js0935/HeliRFID/issues)
- 聯絡: [js0935](https://github.com/js0935)

---

## 🌟 其他

### 時區

專案使用 UTC+8 (亞洲/台北) 時區。

### 程式語言

請儘量使用繁體中文進行溝通。

### 授權

您的貢獻將使用與專案相同的 MIT 授權。

---

感謝您的貢獻！🎉
