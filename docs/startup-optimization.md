# 首页冷启动优化记录

记录时间：2026-08-14  
对象：`MainActivity` 冷启动白屏 / 首屏 inflate 过慢  
计时零点：`HahaApplication.attachBaseContext()` 里 `TimeMonitor.startMonitor()`（**不含**
`System.loadLibrary("GPSTest")`）

本文所有耗时均来自 `TimeMonitor`（Logcat tag：`TimeMonitor`），单位毫秒。不同次冷启动有几十毫秒抖动，对比时看趋势与差值，不要死盯个位数。

---

## 1. 现象与定性

冷启动点图标后首页长时间白屏。按窗口生命周期拆成三段：

| 阶段             | 用户看到什么                        | 谁在画                     |
|----------------|-------------------------------|-------------------------|
| Preview Window | 点图标后立刻出现的那一帧                  | 主题 `windowBackground`   |
| 首帧绘制           | `setContentView` 之后第一次 `draw` | Activity 布局             |
| 数据就绪           | 控件可点                          | `initView` / `initData` |

排查结论：

- **Application 不是主因**（`ApplicationCreate` 始终约 36–48ms）。
- **`initView` / `initData` 不是主因**（合计约 5ms）。
- 主因是 **首页布局 inflate 过重**，叠加 **Splash 临时关闭后 Preview Window 无有效背景**。

---

## 2. 分析手段

按投入产出使用过的工具：

1. 自研 `TimeMonitor` 生命周期打点（主手段）
2. 改 `windowBackground` 颜色，区分「预览窗白」还是「内容白」
3. `adb shell am start -W` / Logcat `Displayed`（系统首帧）
4. 可选：CPU Profiler、Perfetto、StrictMode、`UiPerfMonitor`

建议过滤：

```bash
adb logcat -v time | grep -E "TimeMonitor|Displayed|HahaApplication|MainActivity"
```

---

## 3. 根因（优化前）

### 3.1 同一张 `activity_main` 被 inflate 两次

```
BaseActivity.onCreate
  └─ setContentView(getLayoutId())          // 第 1 次
BaseMvvmActivity.onCreate
  └─ DataBindingUtil.setContentView(...)    // 第 2 次，把第 1 次换掉
```

MVVM 只需要 Binding + ViewModel，不需要两次 `setContentView`。

### 3.2 布局本身偏重

- 外层 `ConstraintLayout` 只包一个 `match_parent` 的 `ScrollView`（约束空转）
- 内层再套 `ConstraintLayout` 竖排 20+ Button
- `CircleProgressView` / `PieChartView` 自定义 View 同步创建（`gone` 也会 inflate）

### 3.3 主题预览窗

Splash（`Theme.App.Starting`）被临时关掉；`Theme.GPSTest.NoActionBar` 原先无 `parent`、无
`windowBackground`，Preview Window 默认白底。

---

## 4. 优化步骤与数据

计时口径：

- **同步方案**：看 `AppStartActivity_start`（`onStart`）
- **Async 方案**：`onStart` 时内容尚未就绪，看 `AppStartActivity_contentReady`
- **inflate 段**：`inflateContentView_before` → `setStatusBarColor_before`

### 4.1 基线：双 `setContentView`（优化前）

| Tag                          | 耗时 (ms) |
|------------------------------|---------|
| `ApplicationCreate`          | 37      |
| `AppStartActivity_create`    | 126     |
| `BaseMvvmActivity_create`    | 444     |
| `initView_before`            | 677     |
| `initData_before`            | 678     |
| `createOver`                 | 682     |
| **`AppStartActivity_start`** | **687** |

差值：

- `create(126)` → `BaseMvvmActivity_create(444)`：**~318ms**（第一次 `setContentView`）
- `BaseMvvmActivity_create` → `initView_before`：**~233ms**（第二次 DataBinding inflate）
- 两次 inflate 合计约 **550ms**
- `initView` + `initData` ≈ **5ms**

另一次粗打点：`createOver=539`，`start=545`（设备抖动，量级一致）。

### 4.2 去重：模板方法 `inflateContentView()`，只 inflate 一次

`BaseActivity` 改为可覆盖的 `inflateContentView()`；`BaseMvvmActivity` 只做一次
`DataBindingUtil.setContentView`。

| Tag                               | 耗时 (ms)  | 相对基线               |
|-----------------------------------|----------|--------------------|
| `ApplicationCreate`               | 36–48    | 持平                 |
| `inflate_before` → `setStatusBar` | **~293** | 去掉第二次 inflate      |
| **`AppStartActivity_start`**      | **468**  | **-219ms**（相对 687） |

另一次：`start=508`（`createOver=502`）。主线程 `onCreate` 不再做两次布局。

### 4.3 ViewStub 延迟加载自定义重控件

`CircleProgressView` / `PieChartView` 抽到：

- `view_stub_circle_progress.xml`
- `view_stub_pie_chart.xml`

首页用 `FrameLayout` 包 `ViewStub`，`initView` 里 `root.post { inflateHeavyCustomViews() }`，首帧后再
`inflate()`。

| Tag                          | 耗时 (ms)  | 相对上一阶段             |
|------------------------------|----------|--------------------|
| inflate 段                    | **~197** | **-96ms**          |
| **`AppStartActivity_start`** | **354**  | **-114ms**（相对 468） |

### 4.4 外层 ConstraintLayout → FrameLayout

外层只负责包 `ScrollView` + 给 `addStatusBarView()` 当容器（`id/cl_main`），Constraint 求解是浪费。

结构：

```
FrameLayout#cl_main
  └─ ScrollView
       └─ ConstraintLayout   ← 内层竖排先保留；gone 未改
```

Kotlin 零改动（`getRootViewId()` 仍指向 `cl_main`）。

| Tag                          | 耗时 (ms)  | 相对上一阶段            |
|------------------------------|----------|-------------------|
| inflate 段                    | **~171** | **-26ms**         |
| **`AppStartActivity_start`** | **342**  | **-12ms**（相对 354） |

### 4.5 AsyncLayoutInflater + `windowBackground`（当前方案）

仅 `MainActivity`：`shouldInflateContentInOnCreate() = false`，`extraConfig()` 里后台 inflate；回调在*
*主线程** `DataBindingUtil.bind` + `setContentView` + `onContentReady()`。

异步等待期间用主题 `android:windowBackground`（**不用 Splash**）。  
`registerForActivityResult` 仍在 `onCreate`（必须在 STARTED 前）。

| Tag                                   | 耗时 (ms) | 含义                                 |
|---------------------------------------|---------|------------------------------------|
| `ApplicationCreate`                   | 43      | 持平                                 |
| `AsyncLayoutInflater_start`           | 146     | 丢到后台                               |
| `inflate_before` → `setNavigationBar` | **~1**  | **主线程同步 inflate 已跳过**              |
| `callback`                            | 411     | 后台 inflate + 调度 ≈ 265ms            |
| `bind_done`                           | 462     | 主线程 bind + `setContentView` ≈ 51ms |
| `createOver`                          | 479     | init 仍很快                           |
| **`contentReady`**                    | **539** | 内容可交互的墙钟时间                         |

结论：

- **主线程 `onCreate` 不再被布局堵住**（这是 Async 的收益）。
- **内容就绪墙钟时间并不更短**（539ms vs 同步方案的 342ms），因为多了线程调度，且 `setContentView`
  仍回主线程。
- `Displayed` 可能早于业务 UI（先画出 `windowBackground`），属预期。

---

## 5. 汇总对比

| 阶段                     | inflate（主线程）                       | 内容就绪口径                 | 相对最初 687ms |
|------------------------|------------------------------------|------------------------|------------|
| 0. 双 setContentView    | ~550ms（两次）                         | start **687ms**        | 基线         |
| 1. 只 inflate 一次        | ~293ms                             | start **468ms**        | **-32%**   |
| 2. ViewStub 重控件        | ~197ms                             | start **354ms**        | **-48%**   |
| 3. 外层改 FrameLayout     | ~171ms                             | start **342ms**        | **-50%**   |
| 4. AsyncLayoutInflater | 主线程 inflate ≈ 0；墙钟 inflate ≈ 265ms | contentReady **539ms** | 主线程解耦，墙钟不优 |

收益最大的两步：**去掉第二次 inflate（约 180–220ms）**、**ViewStub 自定义 View（约 100ms）**。  
外层 FrameLayout 是稳定小收益。  
Async 换的是等待形态，不是更短的 `contentReady`。

```
687ms  ████████████████████████████  双 inflate
468ms  ███████████████████           去重
354ms  ██████████████                ViewStub
342ms  █████████████                 FrameLayout（同步方案最优点）
539ms  ██████████████████████        Async 内容就绪（主线程已释放）
```

---

## 6. 当前代码结构

### 6.1 启动链路（首页）

```
HahaApplication.attachBaseContext  → TimeMonitor.start
HahaApplication.onCreate           → ApplicationCreate
MainActivity.onCreate
  setTheme(Theme.GPSTest.NoActionBar)   // windowBackground 先亮
  extraConfig → AsyncLayoutInflater.inflate   // 后台
  跳过同步 inflateContentView
onStart / onResume                 // binding 可能尚未就绪
[主线程回调]
  DataBindingUtil.bind + setContentView
  onContentReady → initView / initData
  root.post → ViewStub inflate 自定义 View
  TimeMonitor.end(contentReady)
```

### 6.2 关键文件

| 文件                                                          | 作用                                                                                  |
|-------------------------------------------------------------|-------------------------------------------------------------------------------------|
| `BaseActivity.kt`                                           | `inflateContentView()` / `shouldInflateContentInOnCreate()`                         |
| `BaseMvvmActivity.kt`                                       | 同步：`DataBindingUtil.setContentView`；异步：`bindInflatedContentView` + `onContentReady` |
| `MainActivity.kt`                                           | 首页异步 inflate；`inflateHeavyCustomViews()`                                            |
| `activity_main.xml`                                         | `FrameLayout` + `ScrollView` + 内层 Constraint；ViewStub 容器                            |
| `view_stub_circle_progress.xml` / `view_stub_pie_chart.xml` | 延迟加载的自定义 View                                                                       |
| `themes.xml`                                                | `Theme.GPSTest.NoActionBar` 的 `windowBackground`                                    |
| `TimeMonitor.kt`                                            | 打点；`end()` 会把 HashMap 再打一遍，不是又跑一轮                                                   |

其它页面默认 `shouldInflateContentInOnCreate() = true`，仍走同步 DataBinding inflate。

### 6.3 读 TimeMonitor 的注意点

- `end()` 之后同一批 tag 会再打印一次，忽略重复即可。
- 计时从 `attachBaseContext` 开始，**统计不到 so 加载**。
- 异步方案不要拿 `AppStartActivity_start` 和优化前对比，用 `AppStartActivity_contentReady`。

---

## 7. 未做 / 后续可选项

按性价比：

1. **内层 Constraint 改 `LinearLayout(vertical)`**（竖排列表不需要约束求解）
2. **入口改 RecyclerView**（首屏只创建可见 item，inflate 有望到 100ms 内）
3. 恢复 Splash（`Theme.App.Starting`），或把 `windowBackground` 换成品牌图（体感白屏）
4. Release 包对比（Debug + LeakCanary 会拉长数字）
5. `am start -W` / Perfetto 把 so 加载、进程创建补进统计

不建议：为了「contentReady 数字更好看」而保留 Async——若更在意首屏可点时间，同步 inflate（阶段 3，~
342ms）更直观。

---

## 8. 一句话结论

首页白屏主因是 **布局被加载两遍 + 自定义 View 过重 + 无 Splash/无 windowBackground**。  
去掉双 inflate 和 ViewStub 后，同步路径从约 **687ms 降到约 342ms**；Async 让 **主线程 onCreate 几乎不再卡
inflate**，但内容就绪墙钟约 **539ms**，适合「先亮窗口、再出控件」，不适合作为最短可交互时间方案。
