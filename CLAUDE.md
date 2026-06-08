# NewBlackbox 项目指南

## 项目概述

NewBlackbox 是一个 Android 虚拟引擎/沙箱框架，能够在不实际安装 APK 的情况下克隆和运行 Android 应用。它通过代理（Proxy）机制拦截和转发虚拟应用的四大组件（Activity、Service、ContentProvider、BroadcastReceiver），使多个虚拟应用在同一宿主进程中运行。

版本: 3.0.1-alpha  
包名: com.vspace  
支持 Android 版本: 5.0 ~ 14.0 (API 21-34)  
协议: Apache License 2.0

## 技术栈

- 语言: Kotlin + Java + C++ (NDK)
- 构建系统: Gradle 8.3.0 (Kotlin DSL)
- compileSdk: 34, targetSdk: 28, minSdk: 23
- NDK 版本: 24.0.8215888
- CMake 版本: 3.22.1
- JVM 目标: Java 17
- 支持 ABI: armeabi-v7a, arm64-v8a

### 核心依赖

| 库 | 版本 | 用途 |
|---|---|---|
| Pine | 0.2.8 / 0.0.9 | Xposed 兼容 Hook 框架（canyie 出品） |
| ShadowHook | 1.0.8 | 字节跳动出品的 SO Hook 库 |
| AndroidHiddenApiBypass | 4.3 | 绕过 Android 隐藏 API 限制（LSPosed 出品） |
| xCrash | 3.0.0 | 奇异果崩溃收集库 |

## 项目结构

```
NewBlackbox/
├── app/                          # 宿主应用模块（UI 层）
│   ├── src/main/
│   │   ├── AndroidManifest.xml   # 宿主 Activity 声明（WelcomeActivity, ListActivity 等）
│   │   ├── java/                 # 宿主 Java/Kotlin 代码
│   │   └── res/                  # UI 资源
│   └── build.gradle.kts          # applicationId: com.vspace
│
├── Bcore/                        # 核心引擎模块（library）
│   ├── src/main/
│   │   ├── AndroidManifest.xml   # 虚拟引擎声明（110KB，包含大量 Proxy 组件）
│   │   ├── java/
│   │   │   ├── com/vcore/        # 核心引擎代码
│   │   │   │   ├── BlackBoxCore.java   # 引擎主入口（30KB）
│   │   │   │   ├── core/         # 核心系统服务
│   │   │   │   ├── proxy/        # 四大组件代理实现
│   │   │   │   ├── fake/         # 系统 API 伪造层
│   │   │   │   ├── jnihook/      # JNI Hook 实现
│   │   │   │   ├── entity/       # 数据实体
│   │   │   │   └── utils/        # 工具类
│   │   │   ├── android/          # Android 框架层伪造
│   │   │   ├── black/            # 反射辅助（Reflector.java）
│   │   │   │   └── android/      # Android 内部类反射封装（accounts/app/bluetooth/content/os 等）
│   │   │   └── com/vcore/        # 扩展功能
│   │   ├── cpp/                  # Native 层
│   │   │   ├── BoxCore.cpp/.h    # JNI 入口
│   │   │   ├── IO.cpp/.h         # IO 操作 Hook
│   │   │   ├── Hook/             # 系统调用 Hook
│   │   │   │   ├── LinuxHook.cpp         # Linux 系统调用 Hook
│   │   │   │   ├── RuntimeHook.cpp       # ART Runtime Hook
│   │   │   │   ├── SystemPropertiesHook.cpp  # 系统属性 Hook
│   │   │   │   ├── UnixFileSystemHook.cpp    # 文件系统 Hook
│   │   │   │   ├── VMClassLoaderHook.cpp    # 类加载器 Hook
│   │   │   │   └── BinderHook.cpp        # Binder 通信 Hook
│   │   │   └── JniHook/          # JNI 函数 Hook
│   │   └── aidl/                 # AIDL 接口定义
│   └── build.gradle.kts          # namespace: com.vcore
│
├── assets/                       # 项目资源（截图/GIF）
├── build.gradle.kts              # 根构建配置
├── settings.gradle.kts           # 模块声明
└── gradle.properties             # Gradle 属性
```

## 构建说明

```bash
# 调试构建
./gradlew assembleDebug

# 发布构建
./gradlew assembleRelease

# 输出路径
# app/build/outputs/apk/debug/app-armeabi-v7a-debug.apk
# app/build/outputs/apk/debug/app-arm64-v8a-debug.apk
```

注意: 发布构建使用 vcore.jks 签名，调试和发布均启用 V1-V4 签名。

## 虚拟引擎核心原理

### 代理机制（Proxy Pattern）

NewBlackbox 的核心是**组件代理**。在 AndroidManifest.xml 中预注册了 50 个进程槽（p0-p49），每个进程包含:

- **ProxyActivity$P0~P49**: Activity 代理，每个运行在独立进程 :pN
- **TransparentProxyActivity$P0~P49**: 透明 Activity 代理（用于 Dialog 样式）
- **ProxyPendingActivity$P0~P49**: PendingIntent 代理
- **ProxyService$P0~P49**: Service 代理
- **ProxyContentProvider$P0~P49**: ContentProvider 代理

虚拟应用启动时，引擎将其 Intent 重定向到对应的 Proxy 组件，由 Proxy 组件在内部创建和管理真实的虚拟组件。

### Native Hook 层

C++ 层负责底层系统调用拦截:

1. **LinuxHook**: Hook `open`, `stat`, `access` 等文件操作，实现虚拟文件系统路径重定向
2. **UnixFileSystemHook**: Hook Unix 文件系统操作
3. **SystemPropertiesHook**: Hook `__system_property_get`，伪造设备属性（型号、IMEI 等）
4. **RuntimeHook**: Hook ART Runtime 内部函数
5. **VMClassLoaderHook**: Hook 类加载器，使虚拟应用能加载其私有 DEX
6. **BinderHook**: Hook Binder IPC，拦截系统服务调用

使用 ShadowHook 作为底层 inline hook 框架。

### Java 层伪造

`black.android.*` 包通过反射访问 Android 内部 API，伪造以下系统信息:

- 设备信息（Build, Settings.Secure 等）
- 应用信息（PackageManager 返回值）
- 账户信息（AccountManager）
- 位置信息（LocationManager）
- 传感器数据
- 网络状态
- 蓝牙状态

### 与 BlackBox / VirtualApp 的关系

- **VirtualApp**: 本项目的架构原型（Credits 中明确致谢），采用相同的代理组件模式
- **BlackBox**: VirtualApp 的开源衍生版本，NewBlackbox 在此基础上进行现代化改造
- 主要改进: 升级到 Gradle Kotlin DSL、引入 Pine 替代部分 Xposed 功能、集成 ShadowHook、支持 Android 14

## 逆向分析要点

### 关键入口

- `BlackBoxCore.get().doAttachBaseContext()`: 引擎初始化入口
- `BlackBoxCore.get().doCreate()`: 引擎创建完成
- `BlackBoxCore.get().installPackageAsUser()`: 安装虚拟应用
- `BlackBoxCore.get().launchApk()`: 启动虚拟应用

### Hook 点定位

- 虚拟应用的系统调用经过 Native Hook 层重定向，分析时需关注 `Bcore/src/main/cpp/Hook/` 目录
- Java 层系统 API 代理集中在 `com.vcore.fake.*` 包
- 反射调用集中在 `black.Reflector` 类

### 常见检测与绕过方向

虚拟引擎检测通常针对:
1. 进程名检测 - 检查是否运行在 :pN 进程中
2. 文件路径检测 - 检查 /data/data 是否被重定向
3. Binder 接口检测 - 检查系统服务是否被代理
4. 设备信息一致性检测 - 多个虚拟应用共享宿主设备信息

## 已知问题

- 进程死亡重启后会产生重复的 Activity 和进程
- ContentProvider 的 getType 调用会崩溃
- 静态广播测试失败
- 应用多进程行为不符合预期（应该是单进程包含多个 Activity）

## 联系方式

Telegram: https://t.me/newblackboxa
