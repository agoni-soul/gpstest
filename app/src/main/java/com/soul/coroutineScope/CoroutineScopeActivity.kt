package com.soul.coroutineScope

import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.load.Transformation
import com.soul.base.BaseMvvmActivity
import com.soul.gpstest.R
import com.soul.gpstest.databinding.ActivityCoroutineScopeBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.flow.*
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

/**
 * <pre>
 *     author : yangzy33
 *     e-mail : yangzy33@midea.com
 *     time   : 2022/10/25
 *     desc   :
 *     version: 1.0
 * </pre>
 */
class CoroutineScopeActivity: BaseMvvmActivity<ActivityCoroutineScopeBinding, CoroutineScopeViewModel>() {

    private val mBtnStart: Button by lazy {
        findViewById(R.id.btn_start_coroutine_scope)
    }

    private val mBtnCancel: Button by lazy {
        findViewById(R.id.btn_cancel_coroutine_scope)
    }

    private val mTvFirstScope: TextView by lazy {
        findViewById(R.id.tv_coroutine_scope_first)
    }

    private val mTvSecondScope: TextView by lazy {
        findViewById(R.id.tv_coroutine_scope_second)
    }

    // LeakThread 定义为 CoroutineScopeActivity 的内部类
    class LeakThread : Thread() {
        override fun run() {
            super.run()
            try {
                //线程内耗时操作
                sleep(6 * 60 * 1000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    override fun getViewModelClass(): Class<CoroutineScopeViewModel> = CoroutineScopeViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_coroutine_scope

    override fun initView() {
        mBtnStart.setOnClickListener {
            mViewModel.startScan()
        }
        mBtnCancel.setOnClickListener {
            mViewModel.stopScan()
        }
        mViewModel.mSsidFirstData.observe(this) {
            mTvFirstScope.text = it
        }
        mViewModel.mSsidSecondData.observe(this) {
            mTvSecondScope.text = it
        }
    }

    override fun initData() {
        println("--------------------\n" + System.currentTimeMillis())
        lifecycleScope.launch {
//            eatGame()
//            test()
        }
        test()
        println(System.currentTimeMillis())
    }

    suspend fun eatGame() {
        coroutineScope {
            val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
            val game = EatGame()
            launch(dispatcher) {
                println("Ready Go!")
                delay(1000)
                game.timeout()
                println("Timeout!")
            }
            launch(dispatcher) {
                while (game.isActive) {
                    delay(60)
                    val food = Math.random()
                    println("[${Thread.currentThread().name} #1] Feed: $food >>>")
                    println("[${Thread.currentThread().name} #1] Complete: ${game.feed("$food")} >>>")
                }
            }

            launch(dispatcher) {
                while (game.isActive) {
                    delay(50)
                    println("[${Thread.currentThread().name} #2] Eat: ${game.eat()} >>>")
                }
            }
        }
    }

    private fun test() {
        main()
//        val leakThread = LeakThread()
//        leakThread.start()
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
        lifecycleScope.launch {
            delay(1000)
            println("lifecycleScope.launch 完成")
        }
        println("lifecycleScope.launch 后的代码")
        Thread.sleep(4000) // 需要等待，否则程序会提前退出
    }

    private fun main3() {
        println("Start of main")
        lifecycleScope.launch(CoroutineName("launch")) {
            println("Start of runBlocking: ${this.coroutineContext}")
            withContext(Dispatchers.IO + CoroutineName("withContext")) {
                // 在协程中延迟1秒
                delay(1000L)
                println("Inside withContext after delay: ${this.coroutineContext}")
            }
            println("End of runBlocking: ${this.coroutineContext}")
            val ay = async(Dispatchers.IO) {  }
            withContext(Dispatchers.IO) {

            }
            ay.await()
        }
        println("End of main")
    }

    private fun main4() = runBlocking {
        try {
            val result = coroutineScope {
                val job1 = launch {
                    delay(2000)
                    println("Job 1 done")
                }
                val job2 = launch {
                    delay(1000)
                    println("Job 2 done")
                }
                "Result" // 这个返回值就是coroutineScope的返回值
            }
            println("CoroutineScope completed with result: $result")
        } catch (e: Exception) {
            println("Caught exception: $e")
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
        val flow = flow {
            for (i in 1..3) {
                delay(100) // 模拟异步操作
                emit(i) // 发射值
            }
        }

        val flow2 = (1 .. 5)
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
        }  catch (e: Exception) {
            println(e.printStackTrace())
        }
        flow2.filter { it % 2 == 0 }.collect { value -> println("asFlow: $value")}
    }

    private fun main9() = runBlocking {
        flow {
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
            .collect {
                value -> println("$value collected on ${Thread.currentThread().name}")
            }
    }

    private fun main10() = runBlocking {
        flow {
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
        val sharedFlow = MutableSharedFlow<Int>(replay = 2, onBufferOverflow = BufferOverflow.DROP_OLDEST)
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
        sharedFlow.tryEmit( 0)//设置初始值

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
        val channel1 = Channel<Int>()

        // 2. 创建一个缓冲通道，容量为10
        val channel2 = Channel<Int>(10)

        // 3. 创建一个Conflated channel
        val channel3 = Channel<Int>(CONFLATED)

        // 4. 创建一个Unlimited channel
        val channel4 = Channel<Int>(UNLIMITED)

        // 使用示例
        launch {
            for (x in 1..5) {
                channel1.send(x)
            }
            channel1.close() // 关闭通道
        }

        launch {
            for (y in channel1) {
                println(y)
            }
        }
    }
}