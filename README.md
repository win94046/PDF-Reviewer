# PDF Reviewer Android App

基於 Kotlin 和 Jetpack Compose 開發的 Android PDF 閱讀器應用程式。

## 當前需要解決的問題

### 🚨 優先修復項目

**從 LINE 開啟 PDF 文件無法正確載入**
- **問題描述**: 當用戶從 LINE 應用程式分享或開啟 PDF 文件時，應用程式無法正確載入 PDF 內容
- **影響範圍**: Intent 處理和 PDF 文件載入功能
- **相關文件**: 
  - `PdfViewerViewModel.kt` - PDF 載入邏輯
  - `MainActivity.kt` - Intent 處理
- **錯誤訊息**: "載入 PDF 時發生錯誤"

## 技術架構

- **語言**: Kotlin 1.9.0
- **UI 框架**: Jetpack Compose with Material 1
- **架構模式**: Clean Architecture + MVVM
- **依賴注入**: Hilt
- **最低 API 版本**: 24 (Android 7.0)
- **目標 API 版本**: 34

## 建置指令

```bash
# 建置 Debug APK
./gradlew assembleDebug

# 安裝到裝置
./gradlew installDebug

# 執行測試
./gradlew test

# 程式碼檢查
./gradlew lint
```

## 專案結構

```
app/
├── src/main/java/com/yukai/pdfrevieweryukai/
│   ├── ui/screens/          # 畫面組件
│   ├── ui/components/       # 可重用元件
│   ├── ui/theme/           # 主題設定
│   └── ui/navigation/      # 導航設定
└── src/main/res/           # 資源文件
```