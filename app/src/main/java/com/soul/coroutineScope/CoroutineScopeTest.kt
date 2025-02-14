package com.soul.coroutineScope

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.reduce
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.io.IOException

/**
 *
 * @author:     haha
 * @date:       2025/2/11
 * Description: 协程学习测试
 *
 **/
class CoroutineScopeTest {
    private val TAG: String = javaClass.simpleName

    fun start() {
        Log.d(TAG, "start()")

        testFlowCompleteInOnCompletion()
    }

    private fun testSelectChannel(): Unit = runBlocking {
        Log.d(TAG, "testSelectChannel()")
        val channels = listOf(Channel<Int>(), Channel<Int>())
        GlobalScope.launch {
            delay(100)
            channels[0].send(200)
        }

        GlobalScope.launch {
            delay(50)
            channels[1].send(100)
        }

        val result = select<Int?> {
            channels.forEach { channel ->
                channel.onReceive { it }
            }
        }
        result?.toString()?.let { Log.d(TAG, it) }
    }

    private fun testCancelAndException(): Unit = runBlocking {
        val job = launch {
            val child = launch {
                try {
                    delay(Long.MAX_VALUE)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    println("Child is cancelled")
                }
            }
            yield()
            println("Cancelling child")
            child.cancelAndJoin()
            yield()
            println("Parent is not cancelled")
        }
        job.join()
    }

    private fun testCancelAndException2(): Unit = runBlocking {
        val handler = CoroutineExceptionHandler { _, exception ->
            println("Caught $exception")
        }

        val job = GlobalScope.launch(handler) {
            launch {
                try {
                    delay(Long.MAX_VALUE)
                } finally {
                    withContext(NonCancellable) {
                        println("From First Msg: Children are cancelled, but exception is not handled until all children terminate ")
                        delay(100)
                        println("The first child finished its non cancellable block")
                    }
                }
            }

            launch {
                try {
                    delay(10)
                    println("Second child throws an exception")
                    throw ArithmeticException()
                } finally {
                    withContext(NonCancellable) {
                        println("From Second Msg: Children are cancelled, but exception is not handled until all children terminate ")
                        delay(100)
                        println("The second child finished its non cancellable block")
                    }
                }
            }
        }
        job.join()
    }

    private fun testExceptionAggregation(): Unit = runBlocking {
        val handler = CoroutineExceptionHandler { _, exception ->
            println("Caught $exception, \n${exception.suppressed.contentToString()}")
        }

        val job = GlobalScope.launch(handler) {

            launch {
                try {
                    delay(Long.MAX_VALUE)
                } finally {
                    throw IllegalAccessException() // 3
                }
            }

            launch {
                try {
                    delay(20000)
                } finally {
                    throw ArithmeticException() // 2
                }
            }

            launch {
                delay(100)
                throw IOException() // 1
            }
        }
        job.join()
    }

    private fun testSupervisorJob(): Unit = runBlocking {
        val supervisor = CoroutineScope(SupervisorJob())
        val job1 = supervisor.launch {
            delay(100)
            println("child first")
            throw IllegalAccessException()
        }
        val job2 = supervisor.launch {
            try {
                delay(Long.MAX_VALUE)
            } finally {
                println("child second finished.")
            }
        }
        joinAll(job1, job2)
    }

    private fun testExceptionPropagation2(): Unit = runBlocking {
        val scope = CoroutineScope(Job())
        val job = scope.launch {
            async {
                throw IllegalAccessException()
                // 如果 async 抛出异常，launch 就会立即抛出异常，而不会调用，await()
            }
        }
        job.join()
    }

    private fun testCoroutineContext(): Unit = runBlocking {
        launch(Dispatchers.Default + CoroutineName("test")) {
            println("I'm working in thread ${Thread.currentThread().name}")
        }
    }

    private fun testCoroutineContextExtent2() = runBlocking<Unit> {
        val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
            println("Caught $exception")
        }
        val scope = CoroutineScope(
            Job() + Dispatchers.Main + coroutineExceptionHandler
        )
        // 新的CoroutineContext = 父类CoroutineContext + Job()
        val job = scope.launch(Dispatchers.IO) {
            // 新协程
            print("handle testCoroutineContextExtent2: ")
            println("I'm working in thread ${Thread.currentThread().name}")
            throw IOException()
        }
        job.start()
    }

    private fun testCoroutineContextExtent(): Unit = runBlocking {
        val scope = CoroutineScope(Job() + Dispatchers.IO + CoroutineName("test"))
        val job = scope.launch {
            println("${coroutineContext[Job]}  ${Thread.currentThread().name}")

            val result = async {
                println("${coroutineContext[Job]}  ${Thread.currentThread().name}")
                "OK"
            }.await()
            println(result)
        }
        job.join()
    }

    private fun simpleFlow4() = flow<Int> {
        for (i in 1 .. 3) {
            delay(1000)
            emit(i)
        }
    }

    private fun testFlowContext2(): Unit = runBlocking {
        simpleFlow4().flowOn(Dispatchers.IO).collect {
            println("Collected $it ${Thread.currentThread().name}")
        }
    }

    private fun events() = (1..3)
        .asFlow()
        .onEach { delay(1000) }
        .flowOn(Dispatchers.Default)

    private fun testFlowLaunch(): Unit = runBlocking {
        events()
            .onEach {
                println("Event: $it ${Thread.currentThread().name}")
            }
            .launchIn(CoroutineScope(Dispatchers.IO))
            .join()
    }

    private fun numbers() = flow<Int> {
        try {
            emit(1)
            emit(2)
            println("stop")
            emit(3)
        } finally {
            println("transform finish")
        }
    }

    private fun testLimitLengthOperator(): Unit = runBlocking {
        numbers().take(2)
            .collect {
                println(it)
            }
    }

    private fun testTerminalOperator(): Unit = runBlocking {
        val sum = (1..5).asFlow()
            .map { it * it }
            .reduce { a, b ->
                a + b
            }
        println(sum)
    }

    private fun testZip2(): Unit = runBlocking {
        val nums = (1..3).asFlow().onEach { delay(300) }
        val strs = flowOf("One", "Two", "Three").onEach { delay(400) }

        val startTime = System.currentTimeMillis()
        nums.zip(strs) { a, b ->
            "$a -> $b"
        }
            .collect {
                println("$it as ${System.currentTimeMillis() - startTime}")
            }
    }

    private fun requestFlow(i: Int) = flow<String> {
        emit("$i:  First")
        delay(500)
        emit("$i: Second")
    }

    private fun testFlatMapConcat(): Unit = runBlocking {
        val startTime = System.currentTimeMillis()
        // Flow<Flow<String>> 二维流转换位一维流，通过flatMapConcat, flatMapMerge, flatMapLatest
        (1..3).asFlow()
            .onEach {
                delay(100)
            }
            .flatMapMerge {
                requestFlow(it)
            }
            .collect {
                println("$it as ${System.currentTimeMillis() - startTime} ms from startTime")
            }
    }

    private fun testFlowException2(): Unit = runBlocking {
        flow {
            emit(1)
            throw ArithmeticException("Div 0")
        }
            .catch { e: Throwable ->
                println("Caught $e")
                // 抛出异常，使用默认值
                emit (10)
            }
            .flowOn(Dispatchers.IO)
            .collect { println(it) }
    }

    private fun testFlowCompleteInOnCompletion(): Unit = runBlocking {
        (0..3)
            .asFlow()
//            .onEach {
//                if (it == 2) {
//                    throw java.lang.ArithmeticException("Div 0")
//                }
//            }
            .onCompletion {
                if (it != null) {
                    println("Flow completed exception")
                } else {
                    println("Done")
                }
            }
            .catch {
                println("Caught $it")
                emit(-1)
            }
            // onCompletion() 也能捕获下游的流，但是程序仍然会报错
            .collect {
                println(it)
                check(it <= 3) {
                    "Caught $it"
                }
            }
    }

    fun stop() {

    }
}
