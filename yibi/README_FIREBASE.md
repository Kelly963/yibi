# Firebase Remote Config 配置指南

## 一、创建 Firebase 项目

### 1. 创建项目
1. 访问 [Firebase Console](https://console.firebase.google.com/)
2. 点击 **"Add project"**
3. 输入项目名称：`yibi`（或你喜欢的名字）
4. 关闭 Google Analytics（可选，个人用不需要）
5. 点击 **"Create project"**

### 2. 添加 Android 应用
1. 在 Firebase Console 中，点击 **Android 图标** 添加 Android 应用
2. 填写应用信息：
   - **Android package name**：`com.yibi`
   - **App nickname**：`一笔`
3. 点击 **"Register app"**

### 3. 下载配置文件
1. 点击 **"Download google-services.json"** 按钮
2. 将下载的 `google-services.json` 文件放到项目根目录的 `app/` 文件夹下
   - 完整路径：`D:\ClawFile\ASum\yibi\app\google-services.json`

### 4. 完成配置
1. 点击 **"Next"** 直到完成
2. Firebase Console 会自动配置完成

---

## 二、配置 Remote Config 参数

### 1. 开启 Remote Config
1. 在 Firebase Console 左侧菜单选择 **"Remote Config"**
2. 点击 **"Add parameter"** 添加以下参数：

### 2. 添加的参数

| 参数名 | 类型 | 示例值 | 说明 |
|--------|------|--------|------|
| `update_enabled` | boolean | `true` | 是否启用更新检查，`false` 则跳过检查 |
| `latest_version_code` | number | `2` | 最新版本号（整型），大于当前版本则提示更新 |
| `latest_version_name` | string | `"2.0.0"` | 最新版本名称（显示给用户） |
| `update_url` | string | `"https://example.com/yibi.apk"` | APK 下载地址 |
| `force_update` | boolean | `false` | 是否强制更新，`true` 则无法跳过 |

### 3. 发布参数
1. 点击页面底部的 **"Publish changes"** 按钮
2. 参数立即生效（可能有几分钟延迟）

---

## 三、当前版本配置

在 `app/build.gradle.kts` 中，当前版本号定义为：

```kotlin
versionCode = 1        // 对应 latest_version_code
versionName = "1.0.0"  // 对应 latest_version_name
```

发布新版本时，只需在 Firebase Console 修改 `latest_version_code` 为 `2`，用户打开 App 就会收到更新提示。

---

## 四、设置 APK 下载地址

### 方案 1：使用云存储（推荐）

**Firebase Storage（免费额度充足）：**
1. Firebase Console → **Storage** → Get started
2. 上传 APK 文件
3. 右键文件 → **Copy download URL**
4. 将 URL 填入 Firebase Remote Config 的 `update_url` 参数

**其他选择：**
- 腾讯云 COS（个人免费额度）
- 阿里云 OSS
- 七牛云存储
- 自己的服务器

### 方案 2：使用分发平台

| 平台 | 免费额度 | 特点 |
|------|----------|------|
| 蒲公英 | 免费 | 国内应用分发，支持二维码 |
| Fir.im | 免费 | 国内应用分发 |
| App Center | 免费 | 微软出品，稳定 |

---

## 五、验证配置

### 1. 编译项目
```powershell
cd "D:\ClawFile\ASum\yibi"
./gradlew assembleDebug
```

### 2. 查看日志
连接手机后，用 Android Studio 的 Logcat 过滤 `UpdateChecker`，可以看到检查结果。

### 3. 强制刷新配置
在 Firebase Console → Remote Config → **Add parameter** → **More** → **Force fetch** 可以立即刷新配置。

---

## 六、常见问题

### Q: 编译报错 "google-services.json not found"
**A:** 确保已将 `google-services.json` 放到 `app/google-services.json` 路径

### Q: 用户没有 Google Play 服务
**A:** Firebase Remote Config 依赖 Google Play 服务。大多数国内手机有精简版，可能不支持。建议在国内市场发布时考虑其他方案（如蒲公英 SDK）。

### Q: 不想用 Firebase，有没有替代方案？
**A:** 可以改用 GitHub Releases API 或自己搭建简单的版本检测接口。

---

## 七、完整参数配置示例

```
update_enabled = true
latest_version_code = 2
latest_version_name = "2.0.0"
update_url = "https://firebasestorage.googleapis.com/xxx/yibi.apk?alt=media"
force_update = false
```

---

## 八、后续维护

发布新版本流程：
1. 编译 APK：`./gradlew assembleRelease`
2. 上传到云存储
3. Firebase Console → 修改 `latest_version_code` 和 `update_url`
4. 点击 **Publish changes**

用户下次打开 App 就会收到更新提示 🎉
