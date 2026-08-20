package com.haha.main.collection;

import android.util.Log;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @auther: haha
 * @Date: 2025/8/17
 * @Detail: 集合测试
 */
public class CollectionTest {
    private final String TAG = this.getClass().getSimpleName();

    public void test() {
        stackTest();
        dequeTest();
    }

    private void stackTest() {
        Deque<Integer> deque = new ArrayDeque<>();
        for (int i = 0; i < 10; i ++) {
            deque.push(i);
        }
        while (!deque.isEmpty()) {
            int temp = deque.pop();
            Log.d(TAG, "stack: value = " + temp);
        }
    }

    private void dequeTest() {
        Deque<Integer> deque = new ArrayDeque<>();
        for (int i = 0; i < 10; i ++) {
            deque.offer(i);
        }
        while (!deque.isEmpty()) {
            int temp = deque.poll();
            Log.d(TAG, "deque: value = " + temp);
        }
    }
}
