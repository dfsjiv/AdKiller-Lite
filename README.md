# AdKiller Lite / 广告杀手 Lite

一个仅在用户选择的 App 内运行的 Android 无障碍辅助工具。它在本机读取无障碍控件树，发现“跳过”“关闭”“关闭广告”或“×”后按规则自动点击。

## 使用

1. 从 GitHub Actions 下载 `AdKiller-Lite-debug` artifact 并安装 APK。
2. 打开应用，选择需要保护的 App。
3. 点击“开启无障碍服务”，在系统设置中启用广告杀手 Lite。
4. 工具只会在已选择 App 中自动触发；没有匹配项时不会操作。

## 隐私与限制

所有识别、规则、统计和日志均保存在设备本地，不上传数据。当前版本只识别无障碍控件树中的精确文字，不进行 OCR、坐标点击、VPN、Root 或广告 SDK 破解。自动点击可能误触，请只选择确实需要保护的 App。

## 云端构建

每次 push 或 pull request 会运行 GitHub Actions：`testDebugUnitTest assembleDebug`，成功后可在 workflow run 的 Artifacts 区域下载 APK。

## 开发

需要 JDK 17 和 Android SDK 35：

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug
```
