# 一笔 - 记账软件

## 项目概述

一款面向安卓平台的轻量级记账应用，采用 Kotlin + Jetpack Compose 开发，数据完全本地存储（Room数据库）。

## 技术栈

- **开发语言**: Kotlin
- **UI框架**: Jetpack Compose（声明式UI）
- **架构模式**: MVVM + Repository模式
- **本地数据库**: Room（SQLite封装）
- **异步处理**: Kotlin Coroutines + Flow
- **依赖注入**: 手动注入（ViewModel构造时传入Repository实例）
- **最低兼容**: API 26+ (Android 8.0)

## 功能模块

### 1. 收支记录模块（可扩展）
- 录入单笔收入/支出交易
- 支持金额、分类、日期、备注字段
- 新增、编辑、删除操作
- 预置餐饮、交通、工资等13个基础分类

### 2. 分类管理模块（可扩展）
- 收入与支出分类目录维护
- 增删改查操作
- 内置分类不可删除
- 支持自定义分类名称及图标

### 3. 数据统计概览模块（不可扩展）
- 按日、周、月维度聚合收支数据
- 分类占比展示（图例列表）
- 收支趋势图（简化柱状图）
- 支出排行列表

## 项目结构

```
yibi/
├── app/
│   ├── src/main/java/com/yibi/
│   │   ├── data/
│   │   │   ├── local/              # Room数据库
│   │   │   │   ├── AppDatabase.kt
│   │   │   │   ├── TransactionDao.kt
│   │   │   │   ├── CategoryDao.kt
│   │   │   │   ├── DatabaseCallback.kt
│   │   │   │   └── entity/
│   │   │   │       ├── TransactionEntity.kt
│   │   │   │       └── CategoryEntity.kt
│   │   │   └── repository/         # Repository实现
│   │   │       ├── TransactionRepository.kt
│   │   │       └── CategoryRepository.kt
│   │   ├── domain/                 # 领域模型 & 可扩展接口
│   │   │   ├── model/
│   │   │   │   ├── Transaction.kt
│   │   │   │   ├── Category.kt
│   │   │   │   ├── TransactionType.kt
│   │   │   │   └── ModuleContext.kt
│   │   │   └── expandable/
│   │   │       ├── ExpandableUnit.kt
│   │   │       ├── IncomeExpenseRecord.kt
│   │   │       └── CategoryManagement.kt
│   │   ├── ui/
│   │   │   ├── theme/              # Compose主题
│   │   │   ├── screen/             # 页面
│   │   │   │   ├── home/HomeScreen.kt
│   │   │   │   ├── record/RecordScreen.kt
│   │   │   │   ├── category/CategoryScreen.kt
│   │   │   │   └── stats/StatsScreen.kt
│   │   │   ├── component/          # 可复用组件
│   │   │   └── viewmodel/          # ViewModel
│   │   ├── MainActivity.kt
│   │   └── YibiApplication.kt
│   └── src/main/res/               # 资源文件
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## 构建说明

```bash
# 在项目根目录执行
./gradlew assembleDebug
```

生成的 APK 位于 `app/build/outputs/apk/debug/app-debug.apk`

## 可扩展性设计

项目严格遵循需求报告中的接口约定：

- `ExpandableUnit`: 可扩展单元基接口
- `IncomeExpenseRecord`: 收支记录扩展接口
- `CategoryManagement`: 分类管理扩展接口

后续功能（预算提醒、多币种、账单导入）可通过实现 `ExpandableUnit` 接口接入。

## 假设项

1. 图表库：需求未指定，使用 Compose Canvas 自绘简化版图表（避免引入第三方库减少依赖）
2. 图标系统：使用 Material Icons 内置图标，通过名称字符串映射
3. 数据备份：通过 Android 自动备份机制，数据库排除在备份外
4. 货币单位：固定为人民币（元）

## 待澄清项

1. 需求未提供详细的UI配色方案，采用绿色主题（#4CAF50）作为品牌色
2. 需求未指定图表库，采用Compose自绘而非MPAndroidChart（减少依赖）
3. 需求未定义分类图标体系，采用Material Icons内置图标
