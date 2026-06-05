# IntelliJ IDEA 社区版运行指南

## 前置条件检查

打开 PowerShell，执行：

```powershell
java -version
```

- 如果显示 `openjdk version "17"` 或类似 → 继续下一步
- 如果没有或版本不对 → 先安装 JDK 17（https://adoptium.net/）

---

## 步骤 1：安装 Android 插件

1. 打开 IntelliJ IDEA
2. `文件(File) → 设置(Settings) → 插件(Plugins)`
3. 搜索 **"Android"**
4. 安装 **Android** 插件（官方插件，提供 Android 项目支持）
5. 重启 IDE

---

## 步骤 2：配置 Android SDK

1. `文件(File) → 设置(Settings) → 外观与行为(Appearance & Behavior) → 系统设置(System Settings) → Android SDK`
2. 如果未安装 SDK：
   - 点击 `Edit` → `+` 添加 SDK
   - 选择安装路径（建议 `D:\Android\Sdk`）
   - 勾选 **Android 8.0 (API 26)** 和 **Android 13/14 (API 33/34)**
   - 点击 OK 下载（约 2-5GB）
3. 记下 SDK 路径，下一步要用

---

## 步骤 3：打开项目

1. `文件(File) → 打开(Open)`
2. 选择 `D:\ClawFile\一笔\yibi`
3. 等待索引完成

---

## 步骤 4：创建 local.properties

项目根目录创建 `local.properties` 文件：

```properties
sdk.dir=D\:\\Android\\Sdk
```

（路径根据你的实际 SDK 安装位置修改）

---

## 步骤 5：创建 Gradle Wrapper

在 IntelliJ 底部打开终端（Terminal），执行：

```bash
# 方式 A：如果系统已安装 Gradle
gradle wrapper --gradle-version 8.2

# 方式 B：手动下载（如果方式 A 失败）
# 1. 下载 https://services.gradle.org/distributions/gradle-8.2-bin.zip
# 2. 解压到项目根目录，重命名为 gradle（不是必须）
# 3. 手动创建 gradlew.bat（见下方）
```

### 手动创建 gradlew.bat（备用）

在项目根目录创建 `gradlew.bat`：

```batch
@echo off
set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto execute

echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo Please set the JAVA_HOME variable in your environment to match the location of your Java installation.
goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo Please set the JAVA_HOME variable in your environment to match the location of your Java installation.
goto fail

:execute
set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%APP_BASE_NAME%" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

:fail
if not "%OS%" == "Windows_NT" goto end
set EXIT_CODE=1
:end
exit /b %EXIT_CODE%
```

同时创建目录 `gradle/wrapper/`，放入 `gradle-wrapper.jar` 和 `gradle-wrapper.properties`：

**gradle-wrapper.properties**：
```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.2-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

`gradle-wrapper.jar` 下载：https://raw.githubusercontent.com/gradle/gradle/v8.2.0/gradle/wrapper/gradle-wrapper.jar

---

## 步骤 6：同步项目

1. 右侧 Gradle 工具窗 → 点击刷新按钮（大象图标）
2. 或按 `Ctrl + Shift + O`
3. 等待同步完成（首次下载依赖约 5-10 分钟）

---

## 步骤 7：运行

### 方式 A：连接真机
1. 手机开启开发者选项 + USB 调试
2. 用 USB 连接电脑
3. 顶部工具栏选择你的设备 → 点击运行（绿色三角形）

### 方式 B：创建模拟器
1. `工具(Tools) → Android → AVD Manager`
2. 点击 `Create Device`
3. 选择 Pixel 6 → 下载系统镜像（API 26+）
4. 启动模拟器 → 点击运行

---

## 常见问题

### Q: 没有 Android 菜单？
确保安装了 Android 插件，且项目被识别为 Android 项目（看 `app/build.gradle.kts` 是否有 `com.android.application` 插件）。

### Q: Gradle Sync 报错 "SDK location not found"？
确认 `local.properties` 文件存在且路径正确，路径分隔符用 `\\` 或 `/`。

### Q: 下载依赖太慢？
在 `gradle.properties` 中添加阿里云镜像：
```properties
systemProp.http.proxyHost=mirrors.cloud.tencent.com
systemProp.https.proxyHost=mirrors.cloud.tencent.com
```
或在 `settings.gradle.kts` 中替换仓库：
```kotlin
repositories {
    maven { url = uri("https://maven.aliyun.com/repository/public") }
    google()
    mavenCentral()
}
```
