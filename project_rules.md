# GPSTest 项目说明与开发规范

个人 Android 实验工程：按功能分包演示系统能力（定位、蓝牙、网络、启动优化等），并沉淀 Activity 基建、MVI
框架、Flutter 混编与自定义 SPI。  
包名：`com.soul.gpstest`（代码根包 `com.soul`）。Application：`com.soul.SoulApplication`。首页：
`com.soul.main.MainActivity`（`singleInstance`）。

详细框架文档：

- [`docs/base-mvvm-activity.md`](docs/base-mvvm-activity.md) — MVVM Activity 基建
- [`docs/mvi-framework.md`](docs/mvi-framework.md) — MVI 单向数据流
- [`docs/startup-optimization.md`](docs/startup-optimization.md) — 首页冷启动优化

---

## 1. 工程与版本

| 项                      | 当前值                                                             |
|------------------------|-----------------------------------------------------------------|
| 工程名                    | GPSTest                                                         |
| Gradle Wrapper         | 8.13-all                                                        |
| Android Gradle Plugin  | 8.13.1                                                          |
| Kotlin                 | 2.1.0（插件 classpath 2.2.0）                                       |
| JDK / JVM target       | 17                                                              |
| compileSdk / targetSdk | 36                                                              |
| minSdk                 | 24                                                              |
| NDK                    | 28.2.13676358（16 KB page 对齐）                                    |
| CMake                  | 3.22.1，native so：`GPSTest`                                      |
| ABI                    | `arm64-v8a`                                                     |
| Flutter 模块             | `fluttertest`（Dart SDK `^3.10.4`）                               |
| 历史 Flutter 对齐          | Flutter SDK 3.38.5 / Dart 3.10.4 / Flutter Gradle Plugin 88.2.0 |

`buildFeatures`：`viewBinding`、`dataBinding`、`buildConfig` 已开；**Compose 未启用**（相关依赖与
`composeOptions` 已注释）。

---

## 2. 模块划分

`settings.gradle` 当前 include：

| 模块                            | 职责                                            |
|-------------------------------|-----------------------------------------------|
| `:app`                        | 宿主 App，业务 Demo 与基建                            |
| `:BluetoothSdk`               | BLE 扫描 / 连接 / GATT / RFCOMM / A2DP            |
| `:ServiceApi`                 | SPI 接口（如 `IUserService`）                      |
| `:ServiceImpl`                | SPI 实现                                        |
| `:ServiceAnnotation`          | `@BindView` / `@OnClick` / `@ServiceImpl` 等注解 |
| `:ServiceAnnotationProcessor` | 注解处理器                                         |
| `:ServiceAnnotationRuntime`   | 运行时绑定辅助                                       |
| `:pluginapp`                  | 插件化 Demo                                      |
| `:flutter` / `:fluttertest`   | Flutter add-to-app（`include_flutter.groovy`）  |

未启用（目录仍在，settings 已注释）：`:starrysky`、`:ServiceRouter*`。

---

## 3. 技术栈（以仓库现状为准）

- **语言**：Kotlin 为主，历史 Java 可保留；**新代码优先 Kotlin**。
- **UI**：XML + DataBinding（根节点必须 `<layout>`）+ ViewBinding；**不要引入 Compose 作为默认 UI**。
- **架构**：
    - 普通页：`BaseMvvmActivity` + `BaseViewModel`（`AndroidViewModel`）
    - 单向数据流页：`BaseMVIActivity` + `BaseMVIViewModel`
- **异步**：Kotlin Coroutines / Flow 为新代码首选；RxJava 1/2/3 为历史代码，新逻辑不要再加 Rx。
- **网络**：Retrofit + OkHttp + Gson/Moshi；MVI 示例走 `mviFrame` 内 Repository。
- **本地存储**：Room 2.5.0（`room-runtime` + `room-ktx`），不要直接操作 SQLite。
- **图片**：Glide 4.15.1。
- **DI**：无 Hilt/Koin；带参 ViewModel 用 `BaseViewModelFactory`。
- **其它**：AndroidX（`android.useAndroidX=true`）、LeakCanary（仅 debug）、ARouter
  API、AutoService、Lottie、Banner、SplashScreen（首页主题暂关）。

禁止：support 库新依赖、为 Demo 页引入 Hilt、默认用 Compose 重写现有 XML 页。

---

## 4. Activity 继承与页面接入

```text
AppCompatActivity
 └── BaseActivity                 // 系统栏、主题、Activity 栈、TimeMonitor、inflate 开关
      └── BaseMvvmActivity<VB, VM>   // DataBinding、ViewModel、权限、onContentReady
           └── BaseMVIActivity<VB, VM, I, S, E>  // State/Effect 收集 + sendIntent
```

| 场景     | 继承                                                                                                                 | 必写                                                            |
|--------|--------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------|
| 普通业务页  | `BaseMvvmActivity<XxxBinding, XxxViewModel>`                                                                       | `getLayoutId` / `getViewModelClass` / `initView` / `initData` |
| 无独立 VM | 同上，VM 用 `BaseViewModel`                                                                                            | 同上                                                            |
| 首页等重布局 | 覆盖 `shouldInflateContentInOnCreate() = false`，`AsyncLayoutInflater` 后 `bindInflatedContentView` + `onContentReady` | 仅 `MainActivity` 当前这样做                                        |
| MVI 页  | `BaseMVIActivity`                                                                                                  | 另加 `render`；点击只 `sendIntent`；副作用放 `handleEffect`              |

约定：

1. 布局根节点必须是 `<layout>`，否则 DataBinding bind 失败。
2. `onContentReady()` 是内容就绪唯一入口；MVI 必须先 `observeMvi` 再 `super`。
3. 权限 `registerForActivityResult` 固定在 `onCreate`，不能等到异步 inflate 回调。
4. 异步 inflate 下 `onResume` 可能早于 bind，访问 Binding 前用 `isBindingInitialized()`。
5. 默认 `ViewModelProvider(this)[clazz]`，仅无参 / `Application` 单参；带 Repository 用
   `BaseViewModelFactory`。
6. 其它页面保持同步 inflate（`shouldInflateContentInOnCreate() = true`），不要随意套 Async。

---

## 5. MVI 约定（新单向流页面必须遵守）

数据流：`View sendIntent(I)` → `ViewModel handleIntent` → `setState` / `sendEffect` → `render` /
`handleEffect`。

| 类型             | 用途                                                 |
|----------------|----------------------------------------------------|
| `IMviIntent`   | 用户意图，View 唯一入口                                     |
| `IMviUiState`  | 可重放状态，**单一 data class**（`isLoading`、列表、error 正交并存） |
| `IMviUiEffect` | Toast / 导航等一次性副作用；无副作用用 `NoUiEffect`               |

硬性规则：

- 不要在 View 直接调业务方法；不要在 `render` 里做只该发生一次的事（`render` 须幂等）。
- Toast / 跳转不要写入 State；Intent 由 Channel 串行消费。
- 收集必须 `repeatOnLifecycle(STARTED)`（基类已做）。
- IO 在 `handleIntent` 链里 `withContext(Dispatchers.IO)`。

示例：`com.soul.mviFrame.main`（用户列表）、`com.soul.mviFrame.data`（数据页）。完整步骤见
`docs/mvi-framework.md`。

---

## 6. `app` 功能包一览

按 `com.soul.*` 分包，一个包一类 Demo，新增能力优先新建包而不是塞进 `main`。

| 包                                                              | 内容                                                     |
|----------------------------------------------------------------|--------------------------------------------------------|
| `base`                                                         | Activity / Fragment / ViewModel 基建、`ActivityCollector` |
| `main`                                                         | 首页入口、TimeMonitor、Glide 预览、广播、插件 Demo                   |
| `mviFrame`                                                     | MVI 基类与示例                                              |
| `gps`                                                          | 定位                                                     |
| `bluetooth`                                                    | BLE UI，依赖 `:BluetoothSdk`                              |
| `wifi` / `network`                                             | Wi-Fi、网络监听与加密                                          |
| `flutter`                                                      | `FlutterIntegrationActivity` + MethodChannel           |
| `volume`                                                       | 音量 / 歌词 / 媒体                                           |
| `waterfall`                                                    | 瀑布流自定义布局                                               |
| `animation` / `scene` / `transparency`                         | 动画、Scene、透明 Activity                                   |
| `liveData` / `coroutineScope` / `room`                         | 组件与协程、Room 示例                                          |
| `recyclerview` / `easyswipemenulayout`                         | 列表与侧滑菜单                                                |
| `remoteviews` / `dynamicTextView` / `imageViewer` / `selector` | 通知栏、动态文字、大图、Banner 选择                                  |
| `log` / `permission` / `pluincore` / `binder`                  | 日志、权限、插件 Hook、Binder                                   |
| `service.accessibility`                                        | 无障碍 Service                                            |
| `ui`                                                           | Dialog、可折叠 TextView 等通用控件                              |

首页按钮跳转到上述 Activity；新增 Demo 需：包 + 布局 + Manifest 注册 + 首页入口。

---

## 7. 启动与性能

- 计时：`SoulApplication.attachBaseContext` 里 `TimeMonitor.startMonitor()`（**不含**
  `System.loadLibrary("GPSTest")`）。Logcat tag：`TimeMonitor`。
- 首页：`shouldInflateContentInOnCreate() = false` + `AsyncLayoutInflater`；等待期靠主题
  `windowBackground`（Splash 临时关闭）。
- 重自定义 View（`CircleProgressView` / `PieChartView`）用 ViewStub，首帧后再 inflate。
- **不要**在 `BaseActivity` + `BaseMvvmActivity` 各 `setContentView` 一次。
- 其它页默认同步 DataBinding inflate。优化细节与数据见 `docs/startup-optimization.md`。

---

## 8. Flutter 混编

- 模块目录：`fluttertest/`，由 `settings.gradle` 的 `include_flutter.groovy` 引入为 `:flutter`。
- Native 入口：`com.soul.flutter.FlutterIntegrationActivity`（`FlutterFragment` + `FlutterChannel` /
  `MethodChannel`）。
- 改 Flutter 侧逻辑放 `fluttertest/lib/`；改宿主嵌入、通道、状态栏适配放 `app/.../flutter/`。
- 不要把 Flutter UI 逻辑写进 Kotlin Activity，通道协议变更需双侧同步。

---

## 9. 代码风格

- **命名**：类 `UpperCamelCase`；函数/变量 `lowerCamelCase`；常量 `UPPER_SNAKE_CASE`；资源按功能前缀（如
  `activity_main.xml`、`string` 按模块拆文件）。
- **文件头**：现有文件保留 `author / time / desc / version` 风格；新公共 API 补 KDoc。
- **格式**：缩进 4 空格；Kotlin official style（`kotlin.code.style=official`）。
- **优先 Kotlin 特性**：空安全、密封类 / `sealed interface`（Intent、Effect）、data class `copy`、扩展函数。
- **包结构**：功能包内再按 `ui` / `adapter` / `model` 拆；MVI 页同包放 `XxxIntent` / `XxxUiState` /
  `XxxUiEffect` / `XxxViewModel` / `XxxActivity`。
- **不要**：为风格统一而把存量 Java 一次性改写成 Kotlin；不要无关重构、不要扩写未要求的 Markdown。

---

## 10. 改代码时的约束（给协作者 / Agent）

1. 只改任务相关文件；不顺手格式化、不删注释掉的历史代码（除非任务要求）。
2. 新页面走现有基类，不要直接继承 `AppCompatActivity`。
3. 需要单向流时用 MVI 基类，不要再手写一套 Channel + 裸 `lifecycleScope.collect`。
4. 依赖版本以根 `build.gradle` 的 `ext` 与模块 `build.gradle` 为准，不要在规范里写与仓库不一致的「理想版本」。
5. Native / NDK / 16 KB 对齐相关改动需同时考虑 `ndkVersion` 与 Flutter/Rive so。
6. Debug 泄漏检测走 `src/debug` 的 `LeakCanaryInstaller`，不要在 `main` 里直接依赖 LeakCanary。
