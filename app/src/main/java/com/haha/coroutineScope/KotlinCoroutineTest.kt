package com.haha.coroutineScope

import com.haha.HahaGlobalCoroutineExceptionHandler
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.reduce
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

/**
 * @auther: haha
 * @Date:   2025/12/29
 * @Detail:
 */
class KotlinCoroutineTest constructor() {
    private val TAG = this::class.java.simpleName

    fun test() {
        getData()
        GlobalScope.launch {
//            runnable()
            main6_1()
//            suspendingExample()
//            flow()
//            sharedFlow()
        }
    }

    fun getData() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = requestUserInfo()
            println("haha,nihao")
        }
    }

    //延时2000毫秒，返回一个String结果
    suspend fun requestUserInfo(): String {
        delay(2000)
        return "result form userInfo"
    }

    private suspend fun doSomething(): String {
        delay(1000L) // 非阻塞的延迟1秒
        return "Hello, World!"
    }

    // 在协程中调用挂起函数
    private fun main1() {
        println("Start of main")

        // 使用runBlocking阻塞主线程
        runBlocking(Dispatchers.IO) {
            // 在协程中延迟1秒
            delay(1000L)
            println("Inside runBlocking after delay")
            val result = doSomething()
            println(result)
        }

        println("End of main")
    }

    private fun main2() {
        // runBlocking - 阻塞式
        println("开始 runBlocking")
        runBlocking {
            delay(1000)
            println("runBlocking 完成")
        }
        println("runBlocking 后的代码")

        // GlobalScope.launch - 非阻塞式
        println("开始 GlobalScope.launch")
        GlobalScope.launch {
            delay(1000)
            println("GlobalScope.launch 完成")
        }
        println("GlobalScope.launch 后的代码")

        // GlobalScope.async - 非阻塞式
        println("开始 GlobalScope.async")
        GlobalScope.async {
            delay(1000)
            println("GlobalScope.async 完成")
        }
        println("GlobalScope.async 后的代码")

        // lifecycleScope.launch - 非阻塞式
        println("开始 lifecycleScope.launch")
        CoroutineScope(Dispatchers.IO).launch {
            delay(1000)
            println("lifecycleScope.launch 完成")
        }
        println("lifecycleScope.launch 后的代码")
        Thread.sleep(4000) // 需要等待，否则程序会提前退出
    }

    private fun main3() {
        println("Start of main")
        CoroutineScope(Dispatchers.IO).launch(CoroutineName("launch")) {
            println("Start of runBlocking: ${this.coroutineContext}")
            withContext(Dispatchers.IO + CoroutineName("withContext")) {
                // 在协程中延迟1秒
                delay(1000L)
                println("Inside withContext after delay: ${this.coroutineContext}")
            }
            println("End of runBlocking: ${this.coroutineContext}")
            val ay = async(Dispatchers.IO) { }
            withContext(Dispatchers.IO) {

            }
            ay.await()
        }
        println("End of main")
    }

    private fun main4() = runBlocking {
        CoroutineScope(Dispatchers.IO).launch {
            println("coroutineScope start")
            try {
                val result = coroutineScope {
                    val job1 = launch {
                        delay(2000)
                        println("Job 1 done")
                    }
                    val job2 = launch {
                        delay(1000)
                        println("Job 2 done")
                        throw Exception("job2 cancel")
                    }
                    "Result" // 这个返回值就是coroutineScope的返回值
                }
                println("CoroutineScope completed with result: $result")
            } catch (e: Exception) {
                println("Caught exception: $e")
            }
            println("coroutineScope end")
        }
    }

    private fun main5() = runBlocking {
        try {
            coroutineScope {
                launch {
                    delay(1000)
                    throw RuntimeException("Failed")
                }
                launch {
                    delay(500)
                    println("Job 1 done")
                }
            }
        } catch (e: Exception) {
            println("Caught exception: $e")
        }
    }

    private fun main6() = runBlocking {
        val exceptionHandler = CoroutineExceptionHandler { _, exception ->
            println("Caught exception: $exception")
        }
        try {
            supervisorScope {
                try {
                    val child1 = launch {
                        // 子协程1
                        println("Child 1 is starting")
                        delay(1000)
                        println("Child 1 is done")
                    }

                    val child2 = async(exceptionHandler) {
                        // 子协程2
                        println("Child 2 is starting")
                        delay(500)
                        throw Exception("Something went wrong in Child 2")
                    }
                    try {
                        // 等待所有子协程完成
                        joinAll(child1, child2)
                    } catch (e: Exception) {
                        println("Caught an exception - joinAll: $e")
                    }
                } catch (e: Exception) {
                    println("Caught an exception - supervisorScope: $e")
                }
            }
        } catch (e: Exception) {
            println("Caught an exception - runBlocking: $e")
        }
    }

    private fun main6_1() = runBlocking {
        val exceptionHandler = CoroutineExceptionHandler { _, exception ->
            println("Caught exception: $exception")
        }
        try {
            supervisorScope {
                print("1")
                val job1 = launch(HahaGlobalCoroutineExceptionHandler()) {//第一个子协程
                    print("2")
                    throw NullPointerException()//抛出空指针异常
                }
                val job2 = launch {//第二个子协程
                    delay(1000)
                    print("3")
                }
                try {//这里try…catch捕获CancellationException
                    job2.join()
                    println("4")//等待第二个子协程完成：
                } catch (e: Exception) {
                    print("5. $e")//捕获第二个协程的取消异常
                }
            }
        } catch (e: Exception) {//捕获父协程的取消异常
            print("6. $e")
        }

        Thread.sleep(3000)//阻塞主线程3秒，以保持JVM存活，等待上面执行完成
    }


    private fun main7() = runBlocking {
        try {
            supervisorScope {
                try {
                    val child1 = launch {
                        // 子协程1
                        println("Child 1 is starting")
                        delay(1000)
                        println("Child 1 is done")
                    }

                    val child2 = async {
                        // 子协程2
                        println("Child 2 is starting")
                        delay(500)
                        throw Exception("Something went wrong in Child 2")
                    }
                    try {
                        // 等待所有子协程完成
                        child2.await()
                    } catch (e: Exception) {
                        println("Caught an exception - await: $e")
                    }
                } catch (e: Exception) {
                    println("Caught an exception - supervisorScope: $e")
                }
            }
        } catch (e: Exception) {
            println("Caught an exception - runBlocking: $e")
        }
    }

    private fun main8() = runBlocking {
        // 创建一个Flow
        val flow = kotlinx.coroutines.flow.flow {
            for (i in 1..3) {
                delay(100) // 模拟异步操作
                emit(i) // 发射值
            }
        }
        Channel<String>(BUFFERED)

        val flow2 = (1..5)
            .asFlow()

//        val flow3

        // 收集Flow
        flow.collect { value -> println("flow: $value") }
        try {
            println("flow.single() = " + flow.single())
        } catch (e: Exception) {
            println(e.printStackTrace())
        }
        try {
            flow.reduce { accumulator, value ->
                val temp = accumulator + value
                println(temp)
                temp
            }
        } catch (e: Exception) {
            println(e.printStackTrace())
        }
        flow2.filter { it % 2 == 0 }.collect { value -> println("asFlow: $value") }
    }

    private fun main9() = runBlocking {
        kotlinx.coroutines.flow.flow {
            for (i in 1..2) {
                Thread.sleep(100) // 模拟耗时的操作，注意这里使用了Thread.sleep，所以应该在IO线程上执行
                println("$i emit on ${Thread.currentThread().name}")
                emit(i)
            }
        }
            .flowOn(Dispatchers.IO)
            .filter {
                println("$it filter on ${Thread.currentThread().name}")
                it % 2 == 0
            }
            .flowOn(Dispatchers.Default)
            .collect { value ->
                println("$value collected on ${Thread.currentThread().name}")
            }
    }

    private fun main10() = runBlocking {
        kotlinx.coroutines.flow.flow {
            for (i in 1..3) {
                delay(100) // 模拟异步生产
                println("emit: $i")
                emit(i)
            }
        }.buffer() // 缓冲，让生产者和消费者可以并发执行
            .collect { value ->
                delay(300) // 模拟耗时处理
                println("collect: $value")
            }
    }

    private fun main11() = runBlocking {
        val sharedFlow =
            MutableSharedFlow<Int>(replay = 2, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        sharedFlow.distinctUntilChanged()

        // 启动一个协程来发射数据
        launch {
            for (i in 1..20) {
                delay(100)
                sharedFlow.emit(i)
                println("Emitted: $i")
            }
        }

        // 等待一段时间后，第一个收集者
        delay(600)
        launch {
            sharedFlow.collect { value -> println("Collector 1: $value") }
        }

        // 第二个收集者
        delay(1250)
        launch {
            sharedFlow.collect { value -> println("Collector 2: $value") }
        }

        // 等待流完成
        delay(2000)
        println("end")
    }

    private fun main12() = runBlocking {
        val stateFlow = MutableStateFlow(0) // 初始值
        val sharedFlow = MutableSharedFlow<Int>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        sharedFlow.tryEmit(0)//设置初始值
        stateFlow.update { current -> current + 1 }

        // 启动一个协程来更新状态
        launch {
            for (i in 1..3) {
                delay(100)
                stateFlow.value = i
                println("Updated state: $i")
            }
        }

        // 等待一段时间后，第一个收集者
        delay(250)
        launch {
            stateFlow.collect { value -> println("Collector 1: $value") }
        }

        // 第二个收集者
        delay(100)
        launch {
            stateFlow.collect { value -> println("Collector 2: $value") }
        }

        // 等待流完成
        delay(1000)
        println("end")
    }

    private fun main13() = runBlocking {
        val counter = AtomicInteger(0)

        // 使用 delay 的协程
        val job1 = launch {
            repeat(5) {
                println("delay协程: 计数 ${counter.incrementAndGet()}")
                delay(100) // 非阻塞延迟
            }
        }

        // 使用 yield 的协程
        val job2 = launch {
            repeat(5) {
                println("yield协程: 计数 ${counter.incrementAndGet()}")
                yield() // 立即让出执行权
            }
        }

        // 在后台线程中使用 Thread.sleep
        val job3 = launch(Dispatchers.IO) {
            repeat(5) {
                println("sleep线程: 计数 ${counter.incrementAndGet()}")
                Thread.sleep(100) // 阻塞当前工作线程
            }
        }

        // 等待所有任务完成
        joinAll(job1, job2, job3)
        println("所有任务完成，最终计数: ${counter.get()}")
    }

    private fun main() = runBlocking {
        // 1. 创建一个Rendezvous channel（默认）
        val channel1 = Channel<Int>(
            capacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
            onUndeliveredElement = { element ->
                println("$element 被废弃")
            })

        // 2. 创建一个缓冲通道，容量为10
        val channel2 = Channel<Int>(10)

        // 3. 创建一个Conflated channel
        val channel3 = Channel<Int>(
            CONFLATED,
            onUndeliveredElement = { element ->
                println("$element 被废弃")
            })

        // 4. 创建一个Unlimited channel
        val channel4 = Channel<Int>(UNLIMITED)

        // 使用示例
        launch {
            for (x in 1..5) {
                delay(200)
                channel3.send(x)
            }
            channel3.close() // 关闭通道
        }

        launch {
            while (true) {
                val element = channel3.receiveCatching().getOrNull() ?: return@launch
                println("Received: $element")
                delay(400)
            }
        }
    }

    suspend fun suspendingExample() {
        coroutineScope {
            launch {
                delay(1000) // 挂起协程，释放线程
                println("Done suspending")
            }
            // 同一线程可以执行其他协程
            launch {
                delay(500)
                println("Another coroutine")
            }
        }
    }

    @Volatile
    private var count = 0

    fun runnable() {
        val countDownLatch = CountDownLatch(1)

        val runnableOdd = Runnable {
            countDownLatch.await()
            while (true) {
                if (count % 2 == 1) {
                    println("runnableOdd: $count")
                    synchronized(this) {
                        count++
                    }
                }
                if (count > 20) {
                    break
                }
            }
        }
        val runnableEven = Runnable {
            countDownLatch.countDown()
            while (true) {
                if (count % 2 == 0) {
                    println("runnableEven: $count")
                    synchronized(this) {
                        count++
                    }
                }
                if (count > 20) {
                    break
                }
            }
        }
        Thread(runnableOdd).start()
        Thread(runnableEven).start()
    }

    suspend fun flow() {
        val channel = Channel<Int>()

        // 线程A：打印奇数
        val jobA = CoroutineScope(Dispatchers.Default).launch {
            for (i in 1..10 step 2) {
                println("Thread A: $i")
                channel.send(i)  // 发送奇数
                delay(100)  // 模拟处理时间
            }
            channel.close()
        }

        // 线程B：打印偶数
        val jobB = CoroutineScope(Dispatchers.Default).launch {
            for (i in 2..10 step 2) {
                val received = channel.receive()  // 接收奇数
                println("Thread B: $i (收到来自A的: $received)")
                delay(100)
            }
        }

        joinAll(jobA, jobB)
    }

    suspend fun sharedFlow() {
        val sharedFlow = MutableSharedFlow<Int>()

        val jobOdd = CoroutineScope(Dispatchers.Default).launch {
            for (i in 1..10 step 2) {
                println("odd: $i")
                sharedFlow.emit(i + 1)
                delay(500)
            }
        }

        val jobEven = CoroutineScope(Dispatchers.IO).launch {
            sharedFlow.collect {
                delay(100)
                println("even: $it")
            }
        }
        jobEven.start()
        jobOdd.start()
        delay(5000)
    }
}