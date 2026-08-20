# MVI Activity 框架说明

记录时间：2026-08-21  
包路径：`com.soul.mviFrame`  
示例页：`MainMVIActivity`、`DataActivity`  
下层基建：[`base-mvvm-activity.md`](./base-mvvm-activity.md)（`BaseMvvmActivity` / `BaseActivity`）

本文描述项目内 **单向数据流（UDF / MVI）** 基类的职责、数据流、生命周期约定，以及新页面接入方式。

---

## 1. 设计目标

| 目标        | 做法                                                          |
|-----------|-------------------------------------------------------------|
| 单向数据流     | View 只发 Intent；ViewModel 只产出 UiState / UiEffect             |
| 状态可重放     | 用单一 `data class` + `StateFlow`，转屏 / 回前台可恢复                  |
| 副作用只消费一次  | Toast、导航等走 `UiEffect`（Channel），不写入 State                    |
| Intent 串行 | Channel 队列消费，避免并发 reduce 竞态                                 |
| 生命周期安全    | `repeatOnLifecycle(STARTED)` 收集，后台自动停                       |
| 复用基建      | `BaseMVIActivity` 继承 `BaseMvvmActivity`，不重复 Binding / 状态栏逻辑 |

数据流：

```text
用户操作
   │
   ▼
View  sendIntent(I)
   │
   ▼
ViewModel  handleIntent(I)
   │
   ├── setState { ... }   → uiState (StateFlow)  → render(S)
   └── sendEffect(...)    → uiEffect (Channel)   → handleEffect(E)
```

---

## 2. 继承关系与文件清单

```text
AppCompatActivity
  └── BaseActivity                         // 状态栏、主题、Activity 栈（详见 base-mvvm-activity.md）
        └── BaseMvvmActivity<VB, VM>       // DataBinding、ViewModel、initView/initData
              └── BaseMVIActivity<VB, VM, I, S, E>
                    // 自动收集 State/Effect + sendIntent
```

| 文件                             | 职责                                                           |
|--------------------------------|--------------------------------------------------------------|
| `base/MviContract.kt`          | Intent / UiState / UiEffect 标记接口；`NoUiEffect` 占位             |
| `base/BaseMVIViewModel.kt`     | Intent 队列、StateFlow、Effect Channel、`setState` / `sendEffect` |
| `base/BaseMVIActivity.kt`      | `STARTED` 时收集；暴露 `render` / `handleEffect` / `sendIntent`    |
| `base/BaseViewModelFactory.kt` | 需要构造参数（Repository 等）时的 Factory；Application 单参可不使用            |

示例（`main` / `data`）：

| 页面   | Intent       | State         | Effect         | ViewModel          | Activity          |
|------|--------------|---------------|----------------|--------------------|-------------------|
| 用户列表 | `MainIntent` | `MainUiState` | `MainUiEffect` | `MainViewModel`    | `MainMVIActivity` |
| 数据页  | `DataIntent` | `DataUiState` | `DataUiEffect` | `DataMVIViewModel` | `DataActivity`    |

---

## 3. 契约层：`MviContract.kt`

三个标记接口本身无方法，只约束泛型边界：

| 类型             | 含义                       | 谁产生 / 谁消费                       |
|----------------|--------------------------|---------------------------------|
| `IMviIntent`   | 用户意图                     | View → ViewModel                |
| `IMviUiState`  | 可持续 UI 状态（单一 data class） | ViewModel → View `render`       |
| `IMviUiEffect` | 一次性副作用                   | ViewModel → View `handleEffect` |
| `NoUiEffect`   | 无副作用页面的 Effect 占位        | —                               |

**为何拆 State / Effect**

- 列表、loading、错误文案等要随配置变更恢复 → 放 `IMviUiState`
- Toast、跳转、Snackbar 只该发生一次 → 放 `IMviUiEffect`，避免转屏后重复弹窗

---

## 4. ViewModel：`BaseMVIViewModel<I, S, E>`

继承 `com.soul.base.BaseViewModel`（`AndroidViewModel`）。

### 4.1 核心成员

| 成员                | 实现                                     | 说明                      |
|-------------------|----------------------------------------|-------------------------|
| `intentChannel`   | `Channel.UNLIMITED`                    | Intent 入队，串行 `collect`  |
| `uiState`         | `MutableStateFlow` → `StateFlow`       | 新订阅者立刻拿到当前值             |
| `uiEffect`        | `Channel.BUFFERED` → `receiveAsFlow()` | 事件消费完即消失                |
| `currentState`    | `_uiState.value`                       | 读当前状态（只读）               |
| `sendIntent(I)`   | `trySend`，失败再挂起 `send`                 | View 唯一入口，主线程可直接调       |
| `handleIntent(I)` | 抽象 `suspend`                           | 子类写业务分支                 |
| `setState { }`    | `_uiState.update`                      | 基于最新 state 做 `copy`     |
| `sendEffect(E)`   | 同 sendIntent 模式                        | 发一次性副作用                 |
| `onIntentError`   | 默认可覆写                                  | `handleIntent` 抛错时的兜底日志 |

`init` 中启动常驻协程：

```text
intentChannel.consumeAsFlow().collect { handleIntent(it) }
```

保证同一时间只处理一个 Intent，避免并发 `setState` 互相覆盖。

### 4.2 `sendIntent` 为何用 `trySend`

```kotlin
fun sendIntent(intent: I) {
    if (!intentChannel.trySend(intent).isSuccess) {
        viewModelScope.launch { intentChannel.send(intent) }
    }
}
```

- `trySend`：不挂起，点击回调里可直接调用，不必再包协程
- 失败（Channel 关闭或偶发写不进）：再在 `viewModelScope` 里挂起 `send`，避免 Intent 丢失
- 当前为 `UNLIMITED`，日常几乎总是 `trySend` 成功

### 4.3 ViewModel 示例（Main）

```kotlin
class MainViewModel(application: Application) :
    BaseMVIViewModel<MainIntent, MainUiState, MainUiEffect>(
        application,
        MainUiState()
    ) {

    override suspend fun handleIntent(intent: MainIntent) {
        when (intent) {
            MainIntent.FetchUser -> fetchUser()
        }
    }

    private suspend fun fetchUser() {
        setState { copy(isLoading = true) }
        try {
            val users = withContext(Dispatchers.IO) { repository.getUsers() }
            setState { copy(isLoading = false, users = users) }
        } catch (e: Exception) {
            setState { copy(isLoading = false) }
            sendEffect(MainUiEffect.ShowToast(e.localizedMessage ?: "请求失败"))
        }
    }
}
```

约定：

- 耗时 IO 放在 `handleIntent` 调用链里，用 `withContext(Dispatchers.IO)`
- 成功 / 失败都通过 `setState` 更新可重放字段；Toast 走 `sendEffect`
- 不要从 Activity 直接调业务方法，一律 `sendIntent`

---

## 5. Activity：`BaseMVIActivity`

泛型：

```text
VB : ViewDataBinding
VM : BaseMVIViewModel<I, S, E>
I  : IMviIntent
S  : IMviUiState
E  : IMviUiEffect
```

### 5.1 子类必须 / 可选实现

| API                    | 必写？                | 职责                          |
|------------------------|--------------------|-----------------------------|
| `getLayoutId()`        | 是（来自 BaseActivity） | 布局                          |
| `getViewModelClass()`  | 是（来自 BaseMvvm）     | ViewModel 类型                |
| `initView()`           | 是                  | 绑控件、点击里 `sendIntent`        |
| `initData()`           | 是                  | 一次性初始化；观察已由基类完成，常可 `= Unit` |
| `render(state)`        | 是                  | 纯根据 state 画 UI              |
| `handleEffect(effect)` | 否                  | Toast / 跳转；默认空实现            |
| `sendIntent(intent)`   | 调用                 | 基类已提供                       |

### 5.2 观察时机与生命周期

```kotlin
override fun onContentReady() {
    observeMvi()          // 先挂收集
    super.onContentReady() // 再 initView / initData
}

private fun observeMvi() {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            launch { mViewModel.uiState.collect { render(it) } }
            launch { mViewModel.uiEffect.collect { handleEffect(it) } }
        }
    }
}
```

要点：

1. **先 observe 再 initView**：保证初始化阶段发出的状态也能被收到；`StateFlow` 会先回放当前值，`render`
   至少跑一次初始状态。
2. **仅 `STARTED` 收集**：`onStop` 后取消，避免后台刷 UI。
3. **回前台再 collect**：`StateFlow` 再推最新 state；`Effect` Channel 有缓冲，回到前台后可能补发未消费事件。

### 5.3 Activity 示例（Main）

```kotlin
class MainMVIActivity : BaseMVIActivity<
        ActivityMviBinding,
        MainViewModel,
        MainIntent,
        MainUiState,
        MainUiEffect
        >() {

    override fun initView() {
        mViewDataBinding.buttonFetchUser.setOnClickListener {
            sendIntent(MainIntent.FetchUser)
        }
    }

    override fun initData() = Unit

    override fun render(state: MainUiState) {
        // 只根据 state 设置 visibility / 列表，不做一次性副作用
    }

    override fun handleEffect(effect: MainUiEffect) {
        when (effect) {
            is MainUiEffect.ShowToast -> Toast.makeText(this, effect.message, Toast.LENGTH_LONG)
                .show()
        }
    }
}
```

---

## 6. 新页面接入步骤

1. **定义契约**（建议同包或 `XxxContract.kt`）

```kotlin
sealed interface XxxIntent : IMviIntent {
    data object Load : XxxIntent
}

data class XxxUiState(
    val isLoading: Boolean = false,
    val items: List<Item> = emptyList()
) : IMviUiState

sealed interface XxxUiEffect : IMviUiEffect {
    data class ShowToast(val message: String) : XxxUiEffect
}
// 无副作用时：E 用 NoUiEffect，可不写 sealed Effect
```

2. **写 ViewModel**  
   继承 `BaseMVIViewModel<XxxIntent, XxxUiState, XxxUiEffect>(app, XxxUiState())`，实现
   `handleIntent`。

3. **写 Activity**  
   继承 `BaseMVIActivity<Binding, VM, Intent, UiState, UiEffect>`，实现 `render`（及可选
   `handleEffect`）。

4. **需要带参构造 ViewModel 时**

```kotlin
ViewModelProvider(this, BaseViewModelFactory { XxxViewModel(repo) })[XxxViewModel::class.java]
```

当前 `BaseMvvmActivity` 默认用无参 / `AndroidViewModel(Application)` 的
`ViewModelProvider(this)[clazz]`；带 Repository 注入时需在子类自行扩展创建方式（或后续再统一 Factory
钩子）。

5. **Manifest 注册 Activity**（如需从外部跳转）。

---

## 7. 一次请求的时序（Main 拉用户）

```text
1. 用户点「请求」
2. Activity: sendIntent(MainIntent.FetchUser)
3. ViewModel: Intent 入队 → handleIntent
4. setState(isLoading=true) → render：ProgressBar 显示
5. IO 请求用户列表
6a 成功: setState(isLoading=false, users=...) → render：列表
6b 失败: setState(isLoading=false) + sendEffect(ShowToast)
        → render 恢复按钮 + handleEffect 弹 Toast
```

---

## 8. 使用约定与注意点

1. **`render` 幂等**  
   同一 state 可能因回前台再 collect 再渲染；不要在 `render` 里做「只该发生一次」的事。

2. **State 用单一 data class**  
   避免 Idle / Loading / Success 密封类互斥导致进 Loading 时丢掉列表。字段正交：`isLoading`、`users`、
   `error` 等可并存。

3. **不要绕过单向流**  
   不在 View 直接调业务方法；不反射改 `_uiState`；副作用不要塞进 State。

4. **后台期间的 Effect**  
   `STARTED` 以下不 collect；Channel 有缓冲，回前台可能补发。若「不可见时丢弃 Toast」，需另加策略（例如
   `DROP_OLDEST` 或可见性判断）。

5. **依赖**
    - `androidx.lifecycle:lifecycle-runtime-ktx`（`repeatOnLifecycle`）
    - `kotlinx-coroutines`（Flow / Channel）

---

## 9. 与改造前对比（摘要）

| 点      | 改造前                              | 改造后                          |
|--------|----------------------------------|------------------------------|
| 继承     | 直接 `AppCompatActivity`           | 挂 `BaseMvvmActivity`         |
| Intent | 各页自建 Channel + `launch { send }` | 基类 `sendIntent`              |
| State  | 密封类互斥                            | 单一 `UiState` data class      |
| Effect | Toast 混在 Error State             | 独立 `UiEffect`                |
| 收集     | 裸 `lifecycleScope.collect`       | `repeatOnLifecycle(STARTED)` |

---

## 10. 相关源码快速索引

```text
app/src/main/java/com/soul/mviFrame/base/
  MviContract.kt
  BaseMVIViewModel.kt
  BaseMVIActivity.kt
  BaseViewModelFactory.kt

app/src/main/java/com/soul/mviFrame/main/     # 完整示例
app/src/main/java/com/soul/mviFrame/data/     # 第二示例

app/src/main/java/com/soul/base/
  BaseMvvmActivity.kt                         # onContentReady 已 open，供 MVI 先挂观察

完整 MVVM 基建说明见 docs/base-mvvm-activity.md
```
