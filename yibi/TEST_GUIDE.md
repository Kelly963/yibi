# 一笔 - 测试指南

## 方式一：Android Studio 运行（推荐）

### 前置条件
1. 安装 Android Studio Hedgehog (2023.1.1) 或更新版本
2. 安装 Android SDK（API 26+）
3. 配置 JDK 17

### 步骤
1. 打开 Android Studio
2. 选择 `File → Open`，导航到 `D:\ClawFile\一笔\yibi` 文件夹
3. 等待 Gradle Sync 完成（首次可能需要下载依赖，约5-10分钟）
4. 连接真机或启动模拟器（API 26+）
5. 点击工具栏的 **Run** 按钮（绿色三角形）或按 `Shift + F10`
6. 应用将编译并安装到设备上

---

## 方式二：命令行构建 APK

### 前置条件
1. 安装 Android SDK Command Line Tools
2. 配置环境变量 `ANDROID_HOME`
3. 安装 JDK 17

### Windows PowerShell 步骤

```powershell
# 1. 进入项目目录
cd D:\ClawFile\一笔\yibi

# 2. 授予 gradlew 执行权限（首次需要）
# 如果 gradlew 不存在，需要创建：
# 从其他项目复制 gradle/wrapper/ 目录，或运行 gradle wrapper

# 3. 构建 Debug APK
.\gradlew.bat assembleDebug

# 4. 安装到已连接的设备
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

### 构建输出位置
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 方式三：使用现有 Gradle Wrapper（如果系统有Gradle）

```powershell
cd D:\ClawFile\一笔\yibi

# 检查是否已有 gradle wrapper
if (-not (Test-Path "gradle\wrapper\gradle-wrapper.jar")) {
    Write-Host "需要创建 Gradle Wrapper，请使用 Android Studio 打开项目"
} else {
    .\gradlew.bat assembleDebug
}
```

---

## 当前项目缺失的文件说明

本项目目前**缺少 Gradle Wrapper**，这是正常情况（需求报告未要求提供）。你需要通过以下任一方式补充：

### 选项A：用 Android Studio 自动创建（最简单）
用 Android Studio 打开项目后，IDE 会自动创建 `gradle/wrapper/` 目录。

### 选项B：手动复制 Gradle Wrapper
从任何其他 Android 项目复制以下文件到本项目根目录：
```
gradle/
  wrapper/
    gradle-wrapper.jar
    gradle-wrapper.properties
gradlew
gradlew.bat
```

### 选项C：使用系统已安装的 Gradle
```powershell
# 确保 gradle 在 PATH 中
gradle wrapper --gradle-version 8.2
```

---

## 功能测试清单

### 首页测试
| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 1 | 打开应用 | 显示首页，底部有"首页/统计/分类"导航 |
| 2 | 查看概览卡片 | 显示本月收入/支出/结余，今日收入/支出 |
| 3 | 点击右下角 "+" 按钮 | 进入记账页面 |
| 4 | 点击列表中的记录 | 进入编辑页面，数据回显正确 |

### 记账测试
| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 1 | 选择"支出"标签，金额输入35.5，选择"餐饮"分类 | 分类高亮显示 |
| 2 | 点击保存 | 返回首页，列表新增记录，统计更新 |
| 3 | 金额输入-10，点击保存 | 显示红色错误提示"金额必须大于0" |
| 4 | 切换"收入"标签 | 分类列表切换为收入分类（工资、奖金等） |

### 分类管理测试
| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 1 | 点击底部"分类" | 显示分类列表，内置分类标记"内置" |
| 2 | 点击右下角 "+" | 弹出添加对话框 |
| 3 | 输入名称"宠物"，选择图标，点击确定 | 列表新增"宠物"分类 |
| 4 | 点击自定义分类的删除按钮 | 弹出确认对话框，确认后删除 |
| 5 | 观察内置分类 | 没有删除/编辑按钮 |

### 统计测试
| 步骤 | 操作 | 预期结果 |
|------|------|---------|
| 1 | 点击底部"统计" | 显示统计页面，默认"月"维度 |
| 2 | 切换"日"/"周"/"月" | 统计数据按对应范围重新计算 |
| 3 | 查看分类占比 | 显示各分类金额和百分比 |
| 4 | 查看支出排行 | 按金额降序排列，前3名绿色高亮 |

---

## 常见问题

### Q1: Gradle Sync 失败？
- 检查 `File → Settings → Build → Build Tools → Gradle` 中的 Gradle JDK 是否为 JDK 17
- 检查 `local.properties` 中 `sdk.dir` 是否指向正确的 Android SDK 路径

### Q2: 编译报错 "Cannot find symbol R"？
- 执行 `Build → Clean Project` 然后 `Build → Rebuild Project`
- 或命令行执行 `.\gradlew.bat clean assembleDebug`

### Q3: 安装后闪退？
- 检查设备 API 版本是否 ≥ 26
- 查看 logcat 日志：`adb logcat -s YibiApplication`

### Q4: Room 数据库升级？
- 当前数据库版本为 1，如需修改表结构，需在 `AppDatabase` 中增加 `version` 并编写 `Migration`
