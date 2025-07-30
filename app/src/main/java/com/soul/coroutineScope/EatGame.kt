package com.soul.coroutineScope

import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.reflect.KMutableProperty0

/**
 *
 * @author:     haha
 * @date:       2025/6/15
 * Description:
 *
 **/
class EatGame {
    private var feedContinuation: Continuation<Int>? = null
    private var eatContinuation: Continuation<String>? = null
    private var eatAttempts = 0

    var isActive: Boolean = true
        private set

    suspend fun eat(): String {
        return if (isActive) suspendCoroutine {
            this.eatContinuation = it
            resumeContinuation(this::feedContinuation, eatAttempts++)
        } else ""
    }

    suspend fun feed(food: String): Int {
        return if (isActive) suspendCoroutine {
            this.feedContinuation = it
            resumeContinuation(this::eatContinuation, food)
        } else -1
    }

    fun timeout() {
        isActive = true
        resumeContinuation(this::feedContinuation, eatAttempts)
        resumeContinuation(this::eatContinuation, "")
    }

    private fun <T> resumeContinuation(
        continuationRef: KMutableProperty0<Continuation<T>?>,
        value: T
    ) {
        val continuation = continuationRef.get()
        continuationRef.set(null)
        continuation?.resume(value)
    }
}