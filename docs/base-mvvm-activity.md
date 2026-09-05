# BaseMvvmActivity 框架说明

记录时间：2026-08-21  
包路径：`com.haha.base`  
上层扩展：[`mvi-framework.md`](./mvi-framework.md)（`BaseMVIActivity`）  
首页开屏 / 异步预热不走本基类，见 [`startup-optimization.md`](./startup-optimization.md)（
`MainActivity` : `BaseActivity`）

本文描述项目内 **MVVM Activity 基类** 的继承关系、启动流程、可覆盖钩子，以及普通页 / 异步首页 / MVI
页的接入方式。

---

## 1. 设计目标

| 目标                     | 做法                                                                 |
|------------------------|--------------------------------------------------------------------|
| 统一 Binding + ViewModel | 泛型 `V : ViewDataBinding`、`VM : BaseViewModel`                      |
| 统一内容就绪入口               | `onContentReady()`：背景、权限、`initView` / `initData`                   |
| 普通页同步 inflate          | `BaseActivity.inflateContentView()` → DataBinding `setContentView` |
| 系统栏与主题下沉               | 由 `BaseActivity` 处理，子类按需覆盖                                         |
| 可选权限封装                 | `registerForActivityResult` 固定在 `onCreate`（早于 STARTED）             |
| 可叠加 MVI                | `BaseMVIActivity` 在 `onContentReady` 里先挂观察再 `super`                |

---

## 2. 继承关系与文件清单

```text
AppCompatActivity
└── BaseActivity                              // 系统栏、主题、Activity 栈、启动打点
    ├── MainActivity                          // 首页专用，不走本基类
    └── BaseMvvmActivity<V, VM>               // DataBinding、ViewModel、权限、initView/initData
        └── BaseMVIActivity<VB, VM, I, S, E>  // Intent / UiState / UiEffect（见 mvi-framework.md）
```

| 文件                          | 职责                                                                  |
|-----------------------------|---------------------------------------------------------------------|
| `base/BaseActivity.kt`      | 布局 inflate 开关、状态栏 / 导航栏、`ActivityCollector`、`TimeMonitor` 打点        |
| `base/BaseMvvmActivity.kt`  | DataBinding、lazy ViewModel、权限 Launcher、`onContentReady`             |
| `base/BaseViewModel.kt`     | `AndroidViewModel` + `LifecycleObserver` + Rx `CompositeDisposable` |
| `base/BaseMvvmFragment.kt`  | Fragment 侧同款 MVVM（无异步 inflate 分支）                                   |
| `base/ActivityCollector.kt` | Activity 栈收集                                                        |

配套（MVI 包，非必须）：

| 文件                                      | 职责                        |
|-----------------------------------------|---------------------------|
| `mviFrame/base/BaseMVIActivity.kt`      | 继承本基类，自动收集 State / Effect |
| `mviFrame/base/BaseMVIViewModel.kt`     | 单向数据流 ViewModel           |
| `mviFrame/base/BaseViewModelFactory.kt` | 带参 ViewModel 工厂           |

---

## 3. 泛型与子类必写 API

```kotlin
abstract class BaseMvvmActivity<V : ViewDataBinding, VM : BaseViewModel> : BaseActivity()
```

| 方法                    | 来源                 | 说明                   |
|-----------------------|--------------------|----------------------|
| `getLayoutId()`       | `BaseActivity`     | 布局资源 id              |
| `getViewModelClass()` | `BaseMvvmActivity` | ViewModel 的 `Class`  |
| `initView()`          | `BaseMvvmActivity` | 控件初始化、点击监听等          |
| `initData()`          | `BaseMvvmActivity` | 数据加载 / 观察 LiveData 等 |

对外常用成员：

| 成员                       | 说明                                                                      |
|--------------------------|-------------------------------------------------------------------------|
| `mViewDataBinding`       | Binding；未初始化时 `error`（须先 inflate / bind）                                |
| `mViewModel`             | lazy 创建；首次访问时 `ViewModelProvider(this)[clazz]`，并注册为 `LifecycleObserver` |
| `isBindingInitialized()` | Binding 是否已就绪                                                           |
| `mContext`               | 来自 `BaseActivity`，`onCreate` 中赋为 `this`                                 |

---

## 4. 启动流程

### 4.1 同步 inflate（默认，绝大多数页面）

```text
BaseActivity.onCreate
  ├─ requestFeature / extraConfig
  ├─ ActivityCollector.addActivity
  ├─ hideTitleAndActionBar
  ├─ inflateContentView()          ← BaseMvvm：DataBinding.setContentView + lifecycleOwner
  ├─ setStatusBar / NavigationBar
  └─ handleNavigationVAndStatusVisibility

BaseMvvmActivity.onCreate
  ├─ （可选）register 权限 Launcher   ← 必须在 STARTED 之前，与 inflate 解耦
  └─ onContentReady()
       ├─ 根布局背景、状态栏占位（按需）
       ├─ 发起权限请求（若启用）
       ├─ initView()
       └─ initData()
```

### 4.2 首页不走本基类

`MainActivity` 只继承 `BaseActivity`，开屏、异步 inflate、权限都写在首页自己。  
不要再往 `BaseMvvmActivity` 加 `bindInflatedContentView` / 异步开关。细节见
[`startup-optimization.md`](./startup-optimization.md)。

---

## 5. Binding 与销毁

| 时机          | 行为                                                        |
|-------------|-----------------------------------------------------------|
| 同步          | `inflateContentView()` → `DataBindingUtil.setContentView` |
| `onDestroy` | `_binding?.unbind()` 并置空，防泄漏                              |

布局根节点须是 `<layout>`，否则 `bind` 会失败。

---

## 6. 可覆盖钩子

### 6.1 来自 `BaseActivity`

| 钩子                                                | 默认                            | 用途                                |
|---------------------------------------------------|-------------------------------|-----------------------------------|
| `shouldInflateContentInOnCreate()`                | `true`                        | 是否在 `onCreate` 同步 inflate         |
| `isShowStatus()` / `isShowNavigation()`           | `true`                        | 状态栏 / 导航栏显隐                       |
| `isBlackStatusText()`                             | `true`                        | 状态栏文字是否深色                         |
| `getStatusBarColor()` / `getNavigationBarColor()` | 透明                            | 系统栏颜色                             |
| `getRootViewId()`                                 | `0`                           | 沉浸时插入状态栏占位的根布局 id                 |
| `requestFeature()`                                | Android 14+ 可切 NoActionBar 主题 | 窗口特性 / 主题                         |
| `extraConfig()`                                   | 空                             | `super.onCreate` 后、inflate 前的额外配置 |
| `onWindowReady()`                                 | 空                             | 标题栏处理之后；首页在此 `setContentView` 容器  |
| `hideTitleAndActionBar()`                         | 隐藏 ActionBar                  | 标题栏                               |

### 6.2 来自 `BaseMvvmActivity`

| 钩子                                | 默认              | 用途                                     |
|-----------------------------------|-----------------|----------------------------------------|
| `defaultBackgroundId()`           | `R.color.white` | 根布局背景                                  |
| `isUsedEncapsulatedPermissions()` | `false`         | 是否启用封装权限申请                             |
| `requestPermissionArray()`        | `emptyArray()`  | 要申请的权限列表                               |
| `handlePermissionResult(Map)`     | 空               | 权限结果回调                                 |
| `onContentReady()`                | 见 §4            | 内容就绪统一入口；MVI 会先 `observeMvi` 再 `super` |

---

## 7. ViewModel 约定

`BaseViewModel`：

- 继承 `AndroidViewModel(Application)`，实现 `LifecycleObserver`
- 持有 `CompositeDisposable`，`onCleared` 时 `dispose`
- Activity 侧首次访问 `mViewModel` 时，在主线程 `lifecycle.addObserver(viewModel)`

创建方式：

- **默认**：`ViewModelProvider(this)[getViewModelClass()]`  
  适用于无参 / 仅 `Application` 构造（`AndroidViewModel`）。
- **带 Repository 等参数**：使用 `mviFrame` 包内 `BaseViewModelFactory`，或在子类自行扩展创建方式（当前基类未内置
  Factory 钩子）。

```kotlin
ViewModelProvider(this, BaseViewModelFactory { XxxViewModel(repo) })[XxxViewModel::class.java]
```

---

## 8. 接入方式

### 8.1 普通 MVVM 页面（多数业务页）

```kotlin
class XxxActivity : BaseMvvmActivity<ActivityXxxBinding, XxxViewModel>() {

    override fun getLayoutId(): Int = R.layout.activity_xxx

    override fun getViewModelClass(): Class<XxxViewModel> = XxxViewModel::class.java

    override fun initView() {
        // mViewDataBinding.xxx ...
    }

    override fun initData() {
        // 观察 LiveData / 拉数 ...
    }
}
```

无独立业务 ViewModel 时可写 `BaseViewModel`（如 `GpsActivity`、`WifiActivity`）。

可选权限：

```kotlin
override fun isUsedEncapsulatedPermissions(): Boolean = true
override fun requestPermissionArray(): Array<String> = arrayOf(Manifest.permission.CAMERA)
override fun handlePermissionResult(permissionResultMap: Map<String, Boolean>) {
    ...
}
```

### 8.2 首页

继承 `BaseActivity`，不要继承本基类。见 [`startup-optimization.md`](./startup-optimization.md)。

### 8.3 MVI 页面

继承 `BaseMVIActivity`，实现 `render` / 可选 `handleEffect`，点击里 `sendIntent`。  
细节与契约见 [`mvi-framework.md`](./mvi-framework.md)。

---

## 9. 与 Fragment 的对应

`BaseMvvmFragment` 同样提供 DataBinding + lazy ViewModel + 权限 + `initView` / `initData`：

- Binding 在 `onCreateView` 中 `DataBindingUtil.inflate`
- 初始化在 `onViewCreated`

---

## 10. 设计要点与注意

1. **分层清晰**：系统 UI（`BaseActivity`）→ Binding/VM（`BaseMvvmActivity`）→ 可选 MVI。首页单独挂在
   `BaseActivity` 下。
2. **内容就绪统一入口**：MVVM / MVI 页走 `onContentReady`。
3. **权限注册时机**：固定在 `onCreate`。
4. **启动可观测**：`TimeMonitor` 打点含 `BaseActivity_*`、`BaseMvvmActivity_*`。

---

## 11. 相关源码快速索引

```text
app/src/main/java/com/haha/base/
  BaseActivity.kt
  BaseMvvmActivity.kt
  BaseViewModel.kt
  BaseMvvmFragment.kt
  ActivityCollector.kt

# 典型用法
app/src/main/java/com/haha/main/MainActivity.kt          # 首页：BaseActivity + 开屏
app/src/main/java/com/haha/volume/ui/VolumeActivity.kt   # 自定义 ViewModel
app/src/main/java/com/haha/gps/GpsActivity.kt            # 直接用 BaseViewModel
app/src/main/java/com/haha/mviFrame/main/MainMVIActivity.kt  # MVI 扩展
```
