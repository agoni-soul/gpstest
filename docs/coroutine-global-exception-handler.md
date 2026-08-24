# 全局协程异常处理器 HahaGlobalCoroutineExceptionHandler

记录时间：2026-08-24  
对象：通过 ServiceLoader（SPI）注册 kotlinx.coroutines 全局 `CoroutineExceptionHandler`  
实现类：`com.haha.HahaGlobalCoroutineExceptionHandler`  
Logcat tag：`HahaGlobalCEH`

---

## 1. 背景与目标

### 1.1 问题

在 Kotlin 协程中，`launch { throw ... }` 若未在协程体内 `try/catch`，且 Context 中没有
`CoroutineExceptionHandler`，异常会交给线程的 `UncaughtExceptionHandler`。在 Android 上通常表现为 *
*Logcat 崩溃栈 + 进程终止**。

局部 Handler 需要在每个 `launch` 或 `CoroutineScope` 上手动挂载，容易遗漏。

### 1.2 方案

kotlinx.coroutines 支持通过 **Java SPI（ServiceLoader）** 注册全局 `CoroutineExceptionHandler`：

- 当协程 Context **没有** 局部 Handler 时，库会自动加载 SPI 中注册的实现类
- 调用 `handleException(context, exception)` 统一打日志
- 适合作为 **兜底**，捕获漏网之鱼

### 1.3 重要限制（必读）

| 能力                               | 是否支持            |
|----------------------------------|-----------------|
| 统一 Logcat 日志                     | ✅               |
| 写入本地文件 / 自动上报                    | ❌（需自行扩展）        |
| 阻止 Android 崩溃                    | ❌               |
| 替代局部 `CoroutineExceptionHandler` | ❌（局部优先，会跳过 SPI） |
| 捕获 `async` 异常（未 `await`）         | ❌               |
| 捕获 `CancellationException`       | ❌               |

Handler 执行完毕后，仍会调用 `Thread.uncaughtExceptionHandler`，Android 默认仍可能崩溃。

---

## 2. 整体架构

```text
协程 launch 内抛出未捕获异常
        │
        ▼
Context 中是否已有 CoroutineExceptionHandler？
        │
   ┌────┴────┐
   有         无
   │          │
   ▼          ▼
局部 Handler   ServiceLoader 读取 SPI 文件
handleException   META-INF/services/kotlinx.coroutines.CoroutineExceptionHandler
   │          │
   │          ▼
   │     Class.forName("com.haha.HahaGlobalCoroutineExceptionHandler")
   │          │
   │          ▼
   │     new HahaGlobalCoroutineExceptionHandler()
   │          │
   │          ▼
   │     handleException(context, exception)  →  Log.e("HahaGlobalCEH", ...)
   │          │
   └────┬─────┘
        ▼
Thread.uncaughtExceptionHandler（Android 上可能崩溃）
```

---

## 3. 实现步骤（本项目已完成的配置）

### 步骤 1：创建 Handler 实现类

**文件路径：**

```
app/src/main/java/com/haha/HahaGlobalCoroutineExceptionHandler.kt
```

**要求：**

1. `public` 类，包名与 SPI 文件中的全类名一致
2. 实现 `kotlinx.coroutines.CoroutineExceptionHandler`
3. 提供 **无参构造**（默认即可，供 ServiceLoader `newInstance`）
4. 实现 `handleException(context, exception)`

**当前实现要点：**

```kotlin
class HahaGlobalCoroutineExceptionHandler : CoroutineExceptionHandler {

    override val key: CoroutineContext.Key<*> = CoroutineExceptionHandler

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        val coroutineName = context[CoroutineName]?.name ?: "unnamed"
        val job = context[Job]
        Log.e(TAG, "coroutine=$coroutineName ...", exception)
    }
}
```

**可获取的数据：**

| 来源                       | 字段                                     | 说明                                    |
|--------------------------|----------------------------------------|---------------------------------------|
| `exception`              | `javaClass` / `message` / `stackTrace` | 异常类型、信息、堆栈                            |
| `exception.cause`        | 根因                                     | 包装异常时使用                               |
| `exception.suppressed`   | 抑制异常                                   | 多子协程失败时的附加异常                          |
| `context[CoroutineName]` | 协程名                                    | 需在 `launch(CoroutineName("xxx"))` 中指定 |
| `context[Job]`           | Job 状态                                 | 失败 Job 及其状态                           |

**日志去向：** 仅输出到 Logcat（tag `HahaGlobalCEH`），**不做本地文件存储**。

---

### 步骤 2：创建 SPI 注册文件

**文件路径：**

```
app/src/main/resources/META-INF/services/kotlinx.coroutines.CoroutineExceptionHandler
```

**文件内容（仅一行，无空格、无注释）：**

```
com.haha.HahaGlobalCoroutineExceptionHandler
```

**说明：**

- 文件名必须是接口的全限定名：`kotlinx.coroutines.CoroutineExceptionHandler`
- 内容为实现类的全限定名
- 可写多行注册多个实现（每行一个类），kotlinx 会依次调用
- 该文件会随 APK 打包进 `META-INF/services/`

**验证 APK 内是否包含：**

```bash
# 构建后解压 APK，或：
unzip -l app/build/outputs/apk/debug/app-debug.apk | grep CoroutineExceptionHandler
```

应能看到：

```
META-INF/services/kotlinx.coroutines.CoroutineExceptionHandler
```

---

### 步骤 3：Gradle 构建任务（可选但推荐）

**文件路径：** `app/build.gradle`

```gradle
task createCoroutineExceptionHandlerServiceFile {
    doLast {
        def serviceDir = new File("$projectDir/src/main/resources/META-INF/services")
        serviceDir.mkdirs()
        def serviceFile = new File(serviceDir, "kotlinx.coroutines.CoroutineExceptionHandler")
        serviceFile.text = "com.haha.HahaGlobalCoroutineExceptionHandler"
    }
}

assemble.dependsOn createCoroutineExceptionHandlerServiceFile
```

**作用：**

- 每次 `assemble` 时确保 SPI 文件存在且类名正确
- 避免手动维护时类名与实现类不一致

**注意：** 项目中 SPI 文件已直接提交到仓库，与 Gradle 任务写入内容一致，二者任选其一即可；同时保留时以仓库内文件为准，assemble
会覆盖为相同内容。

---

### 步骤 4：ProGuard / R8 Keep 规则

**文件路径：** `app/proguard-rules.pro`

```pro
# ServiceLoader 全局 CoroutineExceptionHandler，避免 R8 裁掉无参构造
-keep class com.haha.HahaGlobalCoroutineExceptionHandler { <init>(); }
-keep class kotlinx.coroutines.CoroutineExceptionHandler
```

**为什么需要：**

R8 通过静态分析判断类是否被使用。SPI 通过 **字符串**
`Class.forName("com.haha.HahaGlobalCoroutineExceptionHandler")` 反射创建，R8 看不到直接引用，Release
混淆时可能：

- 删除整个类
- 将类名改为 `a.b`
- 删除无参构造

导致 ServiceLoader 加载失败，全局 Handler **静默失效**。

**各行含义：**

| 规则                                                                       | 作用                                          |
|--------------------------------------------------------------------------|---------------------------------------------|
| `-keep class com.haha.HahaGlobalCoroutineExceptionHandler { <init>(); }` | 保留类名和无参构造，供 `Class.forName` + `newInstance` |
| `-keep class kotlinx.coroutines.CoroutineExceptionHandler`               | 保留接口全名，与 SPI 资源文件名一致                        |

**当前状态：** `release` 构建中 `minifyEnabled false`，规则暂未生效；开启混淆后必须保留。

**额外注意：** 确认 `packagingOptions` 未排除 `META-INF/services/**`（当前仅 exclude
`META-INF/DEPENDENCIES`，无影响）。

---

## 4. 使用方式

### 4.1 方式 A：SPI 自动兜底（推荐用于漏网异常）

**无需手动调用**，只要协程满足：

- 使用 `launch`（不是 `async`）
- 协程体内未 `try/catch` 掉该异常
- Context 中 **没有** 局部 `CoroutineExceptionHandler`

**示例：**

```kotlin
GlobalScope.launch(CoroutineName("fetchUser")) {
    throw NullPointerException("network failed")
}
```

Logcat 过滤：

```bash
adb logcat -v time | grep HahaGlobalCEH
```

预期输出包含协程名、Job、异常类型、message 及完整堆栈。

---

### 4.2 方式 B：显式挂到 launch（当局部 Handler 用）

```kotlin
launch(HahaGlobalCoroutineExceptionHandler()) {
    throw NullPointerException()
}
```

此时 Context 中已有 Handler，**不会**再走 SPI 路径，但会调用同一个类的 `handleException`。

本项目测试代码见 `KotlinCoroutineTest.main6_1()`：

```kotlin
val job1 = launch(HahaGlobalCoroutineExceptionHandler()) {
    throw NullPointerException()
}
```

配合 `supervisorScope`，兄弟协程 `job2` 不会被取消，仍可继续执行。

---

### 4.3 方式 C：局部 Handler（与全局互斥）

```kotlin
val handler = CoroutineExceptionHandler { _, e ->
    println("Caught: $e")
}
launch(handler) { throw NullPointerException() }
```

局部 Handler 存在时，**SPI 全局 Handler 不会被调用**。

若既要防崩溃又要打日志，应使用局部 Handler，或在局部 Handler 内复用相同的日志逻辑。

---

## 5. 何时会 / 不会触发全局 Handler

| 场景                                                          | 触发 HahaGlobalCEH？                              |
|-------------------------------------------------------------|------------------------------------------------|
| `GlobalScope.launch { throw e }`（无局部 CEH）                   | ✅                                              |
| `CoroutineScope(Job()).launch { throw e }`                  | ✅                                              |
| `supervisorScope { launch { throw e } }`（子 launch 无 CEH）    | ✅                                              |
| `launch(HahaGlobalCoroutineExceptionHandler()) { throw e }` | ✅（显式局部，非 SPI）                                  |
| `launch(otherHandler) { throw e }`                          | ❌（走 otherHandler）                              |
| `async { throw e }` 且未 `await()`                            | ❌                                              |
| 协程体内 `try { throw e } catch { }`                            | ❌                                              |
| `CancellationException`                                     | ❌（取消不算失败）                                      |
| `coroutineScope { launch { throw e } }`                     | 异常在 scope 结束时抛出，行为与根 launch 不同，外层需 `try/catch` |

---

## 6. 验证清单

按顺序执行：

1. **确认文件齐全**
    - [ ] `HahaGlobalCoroutineExceptionHandler.kt`
    - [ ] `META-INF/services/kotlinx.coroutines.CoroutineExceptionHandler`
    - [ ] `proguard-rules.pro` keep 规则
    - [ ] `build.gradle` SPI 类名

2. **编写测试协程**（不要加局部 Handler）

   ```kotlin
   GlobalScope.launch(CoroutineName("test-global-ceh")) {
       throw RuntimeException("test global handler")
   }
   ```

3. **运行 App，查看 Logcat**

   ```bash
   adb logcat -v time -s HahaGlobalCEH
   ```

4. **确认日志字段**
    - `coroutine=test-global-ceh`
    - `type=java.lang.RuntimeException`
    - 完整 stack trace

5. **Release 构建验证**（开启 `minifyEnabled true` 后）
    - 确认 APK 内 SPI 文件存在
    - 确认 Logcat 仍能出现 `HahaGlobalCEH`

---

## 7. 与 supervisorScope 的配合

`supervisorScope` / `SupervisorJob` 保证：**一个子协程失败不会取消兄弟协程**。

但 `launch` 未捕获异常仍会触发 Handler（或崩溃），二者独立：

```kotlin
supervisorScope {
    launch { throw NullPointerException() }  // 触发 Handler，但不 cancel 兄弟
    launch {
        delay(1000)
        println("still runs")  // 若进程未因 uncaught 崩溃，仍会执行
    }
}
```

若需 **既捕获异常又不崩溃**，必须在 `launch` 上挂 Handler 或在协程内 `try/catch`，不能仅依赖 SPI 全局兜底。

---

## 8. 扩展建议

若需本地存储或上报，在 `handleException` 中扩展，例如：

```kotlin
override fun handleException(context: CoroutineContext, exception: Throwable) {
    Log.e(TAG, "...", exception)
    // Crashlytics.recordException(exception)
    // 写入 filesDir/crash.log
}
```

注意：IO 操作应尽量简短，避免在异常路径阻塞过久。

---

## 9. 相关文件索引

| 文件                                                                                      | 作用                  |
|-----------------------------------------------------------------------------------------|---------------------|
| `app/src/main/java/com/haha/HahaGlobalCoroutineExceptionHandler.kt`                     | 全局 Handler 实现       |
| `app/src/main/resources/META-INF/services/kotlinx.coroutines.CoroutineExceptionHandler` | SPI 注册              |
| `app/build.gradle`                                                                      | assemble 时生成 SPI 文件 |
| `app/proguard-rules.pro`                                                                | R8 保留规则             |
| `app/src/main/java/com/haha/coroutineScope/KotlinCoroutineTest.kt`                      | `main6_1()` 测试示例    |

---

## 10. 常见问题

**Q：加了全局 Handler，App 还是崩溃？**  
A：设计如此。SPI Handler 只负责记录，之后仍会走 `UncaughtExceptionHandler`。防崩溃需局部 Handler 或协程内
catch。

**Q：Logcat 看不到 HahaGlobalCEH？**  
A：检查是否在同一 `launch` 上挂了其它 `CoroutineExceptionHandler`；检查 SPI 文件类名是否与实现类一致；Release
检查 R8 keep 规则。

**Q：main6_1 里用了 HahaGlobalCoroutineExceptionHandler()，和 SPI 有什么区别？**  
A：显式传入是 **局部 Handler**，Context 已有 CEH，SPI 不会参与；行为相同（都打 Logcat），触发路径不同。

**Q：async 的异常怎么捕获？**  
A：使用 `try { deferred.await() } catch (e: Exception) { }`，不能依赖 CoroutineExceptionHandler。
