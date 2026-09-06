# HahaLearn

个人 Android 实验工程：按功能分包演示系统能力（定位、蓝牙、网络、启动优化等），并沉淀 Activity
基建、MVI、页面路由、组件化 SPI、日志组件，以及 Retrofit / OkHttp / Glide 等源码笔记。

| 项           | 值                                              |
|-------------|------------------------------------------------|
| 包名          | `com.haha.hahalearn`（代码根包 `com.haha`）          |
| Application | `com.haha.HahaApplication`                     |
| 首页          | `com.haha.main.MainActivity`（`singleInstance`） |
| 开发约定        | [project_rules.md](project_rules.md)           |

打开本仓库时，优先从下面「文档索引」进入各主题；流程图均已渲染为 PNG，GitHub / Android Studio 可直接预览。

---

## 环境与版本

| 项                      | 当前值                                      |
|------------------------|------------------------------------------|
| Gradle Wrapper         | 8.13-all                                 |
| Android Gradle Plugin  | 8.13.1                                   |
| Kotlin                 | 2.1.0（插件 classpath 2.2.0）                |
| JDK / JVM target       | 17                                       |
| compileSdk / targetSdk | 36                                       |
| minSdk                 | 24                                       |
| NDK                    | 28.2.13676358（16 KB page 对齐）             |
| CMake                  | 3.22.1，native so：`HahaLearn`             |
| ABI                    | `arm64-v8a`                              |
| Flutter 模块             | `flutterStudy/`（Dart 包名 `flutter_study`） |

UI 为 XML + DataBinding / ViewBinding；**Compose 未启用**。新异步优先 Kotlin Coroutines / Flow。

---

## 怎么跑

本机已装 Android Studio（或命令行 JDK 17 + Android SDK）后：

```bash
./gradlew :app:assembleDebug
```

用 Android Studio 打开本目录，选 `app` 运行到 `arm64-v8a` 设备或模拟器。

依赖仓库默认走阿里云镜像（见根 `build.gradle`）。Gradle 发行包 URL 在
`gradle/wrapper/gradle-wrapper.properties`，**不要随意改镜像或清 wrapper 缓存**，否则会重新下载整包。

---

## 模块划分

`settings.gradle` 当前 include：

| 模块                                                                | 职责                                                |
|-------------------------------------------------------------------|---------------------------------------------------|
| `:app`                                                            | 宿主：业务 Demo。只编译依赖接口 / runtime，不依赖 `ServiceImpl` 源码 |
| `:lib-common`                                                     | `BaseActivity` / `BaseMvvm*` / 主题 / `TimeMonitor` |
| `:shared`                                                         | KMP / Compose Multiplatform 共享层                   |
| `:BluetoothSdk`                                                   | BLE 扫描 / 连接 / GATT / RFCOMM / A2DP                |
| `:DOFLog`                                                         | 独立日志库，入口 `DOFLogUtil`                             |
| `:ServiceRouter`                                                  | 页面路由运行时，门面 `DOFRouter`                            |
| `:ServiceRouterAnnotation` / `:ServiceRouterProcessor`            | `@Route` 与 kapt 生成 `RouteLoader_*`                |
| `:ServiceRouterUtils`                                             | 路由工具                                              |
| `:ServiceRouterPlugin`                                            | includeBuild 插件：打包期 ASM 注入路由表 / SPI 总入口           |
| `:ServiceApi`                                                     | SPI 接口（如 `IUserService`）                          |
| `:ServiceRuntime`                                                 | `ServiceLoader` / `ServiceLoaderHelper`           |
| `:ServiceImpl`                                                    | SPI 实现，宿主 `runtimeOnly` 仅打包                       |
| `:ServiceAnnotation` / `:ServiceAnnotationProcessor`              | `@IServiceLoader` 与生成 `ServiceInit_*`             |
| `:BindViewAnnotation` / `:BindViewProcessor` / `:BindViewRuntime` | 编译期 View 绑定（`@BindView` / `@OnClick`）             |
| `:JdkSpiApi` / `:JdkSpiImpl`                                      | JDK `ServiceLoader` 对照 Demo                       |
| `:pluginapp`                                                      | 插件化 Demo                                          |
| `:flutter` / `flutterStudy/`                                      | Flutter add-to-app                                |

未启用（目录仍在，settings 已注释）：`:starrysky`。

### 自研框架一句话

- **DOFRouter**：编译期收集 `@Route`，打包期插件注入装表，运行期按 path 跳转；未注入时回退扫 dex。
- **ServiceLoader**：编译期写表 → 插件聚合 → 运行时按接口取实现，宿主不 `new` 实现类。
- **DOFLog**：门面 + 多通道 Adapter（Logcat / 磁盘 / 崩溃），`attachBaseContext` 里 `init`。
- **BindView**：注解处理器生成绑定代码，对照 ButterKnife 思路。

`app` 接入路由插件：`id 'com.haha.servicerouter.register'`；kapt 参数 `DOFROUTER_MODULE_NAME` /
`SERVICE_MODULE_NAME`。

---

## Activity 继承

```text
AppCompatActivity
 └── BaseActivity                    // 系统栏、主题、Activity 栈、TimeMonitor
      ├── MainActivity               // 首页：开屏 + 异步预热，不走 MVVM 基类
      └── BaseMvvmActivity<VB, VM>   // 普通页 DataBinding、ViewModel、权限
           └── BaseMVIActivity<VB, VM, I, S, E>
```

| 场景     | 继承                 | 说明                                      |
|--------|--------------------|-----------------------------------------|
| 首页     | `BaseActivity`     | 开屏 / 异步 inflate 只写在本页                   |
| 普通业务页  | `BaseMvvmActivity` | `getLayoutId` / `initView` / `initData` |
| 单向数据流页 | `BaseMVIActivity`  | View 只 `sendIntent`；副作用走 `UiEffect`     |

页面布局根节点须为 `<layout>`
。完整约定见 [docs/base-mvvm-activity.md](docs/base-mvvm-activity.md)、[docs/mvi-framework.md](docs/mvi-framework.md)。

---

## `app` 功能包

按 `com.haha.*` 分包，一个包一类 Demo：

| 包                                                              | 内容                                           |
|----------------------------------------------------------------|----------------------------------------------|
| `base`                                                         | Activity / Fragment / ViewModel 基建           |
| `main`                                                         | 首页、TimeMonitor、网络 / 源码 Demo 入口               |
| `mviFrame`                                                     | MVI 基类与示例                                    |
| `gps`                                                          | 定位（`@Route` 示例页）                             |
| `bluetooth`                                                    | BLE UI，依赖 `:BluetoothSdk`                    |
| `wifi` / `network`                                             | Wi-Fi、网络监听与加密                                |
| `flutter`                                                      | `FlutterIntegrationActivity` + MethodChannel |
| `volume`                                                       | 音量 / 歌词 / 媒体                                 |
| `waterfall`                                                    | 瀑布流                                          |
| `animation` / `scene` / `transparency`                         | 动画、Scene、透明 Activity                         |
| `liveData` / `coroutineScope` / `room`                         | 组件、协程、Room                                   |
| `recyclerview` / `easyswipemenulayout`                         | 列表与侧滑                                        |
| `remoteviews` / `dynamicTextView` / `imageViewer` / `selector` | 通知栏、动态文字、大图、Banner                           |
| `log` / `permission` / `pluincore` / `binder`                  | 日志、权限、插件 Hook、Binder                         |
| `service.accessibility`                                        | 无障碍 Service                                  |
| `ui`                                                           | Dialog、可折叠 TextView 等                        |

首页按钮跳转到上述 Activity。新增 Demo：新包 + 布局 + Manifest + 首页入口。

---

## 文档索引

详细笔记在 `docs/`。内容较多的主题独立成包，配图在各自 `assets/`（PNG 可直接预览，`.mmd` 为源文件）。

### 自研框架

| 主题                               | 文档                                                                             |
|----------------------------------|--------------------------------------------------------------------------------|
| DOFRouter 底层（`@Route` → 装表 → 跳转） | [dofrouter-DOFRouter底层实现.md](docs/dofrouter/dofrouter-DOFRouter底层实现.md)        |
| DOFRouter 优化后实现与对照               | [dofrouter-optimization.md](docs/dofrouter/dofrouter-optimization.md)          |
| 扫 dex vs 插件插桩性能                  | [dofrouter-扫dex与插件插桩性能.md](docs/dofrouter/dofrouter-扫dex与插件插桩性能.md)            |
| ServiceLoader 优化后设计              | [service-optimized-design.md](docs/service-loader/service-optimized-design.md) |
| ServiceLoader 优化前笔记              | [service-legacy-notes.md](docs/service-loader/service-legacy-notes.md)         |
| DOFLog 设计模式与打印流程                 | [doflog-design-and-flow.md](docs/doflog/doflog-design-and-flow.md)             |

### 页面基建与启动

| 主题               | 文档                                                                                  |
|------------------|-------------------------------------------------------------------------------------|
| BaseMvvmActivity | [base-mvvm-activity.md](docs/base-mvvm-activity.md)                                 |
| MVI 单向数据流        | [mvi-framework.md](docs/mvi-framework.md)                                           |
| 首页冷启动优化          | [startup-optimization.md](docs/startup-optimization.md)                             |
| 全局协程异常处理器        | [coroutine-global-exception-handler.md](docs/coroutine-global-exception-handler.md) |

### 网络与三方库源码

| 主题                                     | 文档                                                                                       |
|----------------------------------------|------------------------------------------------------------------------------------------|
| Retrofit 使用与源码（纯 Call）                 | [retrofit-usage-guide.md](docs/retrofit/retrofit-usage-guide.md)                         |
| OkHttp：`newCall` 到读 body               | [okhttp-call-to-response.md](docs/okhttp/okhttp-call-to-response.md)                     |
| HTTPS 上 HTTP/1.1、2、3                   | [https-http-versions.md](docs/https-http-versions.md)                                    |
| MQTT Fixed Header 与收发 Demo             | [mqtt-sender-receiver.md](docs/mqtt/mqtt-sender-receiver.md)                             |
| Glide 缓存与 Engine                       | [glide-cache-engine-notes.md](docs/glide/glide-cache-engine-notes.md)                    |
| RxJava create / subscribe / 线程切换 / map | [rxjava-create-subscribe-operators.md](docs/rxjava/rxjava-create-subscribe-operators.md) |

### JDK 并发

| 主题                 | 文档                                                                  |
|--------------------|---------------------------------------------------------------------|
| AQS                | [aqs-notes.md](docs/aqs-notes.md)                                   |
| ThreadPoolExecutor | [thread-pool-executor-notes.md](docs/thread-pool-executor-notes.md) |

---

## 技术栈（以仓库现状为准）

- **语言**：Kotlin 为主；新代码优先 Kotlin。
- **架构**：普通页 `BaseMvvmActivity`；单向流页 `BaseMVIActivity`。
- **网络**：Retrofit 2.6.1 + OkHttp + Gson/Moshi；HTTP/3 探测用 Cronet。
- **本地存储**：Room 2.5.0、MMKV。
- **图片**：Glide 4.15.1。
- **其它**：LeakCanary（仅 debug）、Lottie、Banner、SplashScreen、Xposed API（compileOnly）。
- **无** Hilt / Koin；带参 ViewModel 用 `BaseViewModelFactory`。

禁止：为 Demo 引入 Hilt、默认用 Compose 重写现有 XML
页。完整规范见 [project_rules.md](project_rules.md)。

---

## Flutter 混编

- 模块目录：`flutterStudy/`，由 `settings.gradle` 的 `include_flutter.groovy` 引入为 `:flutter`。
- Native 入口：`com.haha.flutter.FlutterIntegrationActivity`（`FlutterFragment` + MethodChannel）。
- Flutter 侧改 `flutterStudy/lib/`；宿主嵌入、通道、状态栏适配改 `app/.../flutter/`。
- 通道协议变更需双侧同步。模块自带说明：[flutterStudy/README.md](flutterStudy/README.md)。
