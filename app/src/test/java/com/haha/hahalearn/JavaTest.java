package com.haha.hahalearn;

import java.util.ArrayDeque;

/**
 * @auther: haha
 * @Date: 2026/1/14
 * @Detail:
 */
public class JavaTest {
    @org.junit.Test
    public void test() {
        ArrayDeque<Integer> deque = new ArrayDeque<>();
        deque.offer(1);
        deque.peek();
        deque.poll();
        System.out.println();
    }
}
