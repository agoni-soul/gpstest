package com.soul.recyclerview;

import android.view.View;

import java.util.Stack;

/**
 * @author: haha
 * @date: 2025/4/11
 * Description: RecyclerView回收类
 **/
public class Recycler {
    // 集合 ==》 list hashMap stack数组
    // Stack 一个集合
    //stack数组，要用多个stack来缓存不同类型的item
    private Stack<View>[] views;

    /**
     * 回收池中缓存的item有多种类型，每种类型的item用一个栈来保存
     *
     * @param typeCount
     */
    public Recycler(int typeCount) {
        views = new Stack[typeCount];
    }

    /**
     * 获取缓存的Item
     *
     * @param type Item类型
     * @return
     */
    public View getRecyclerView(int type) {
        try {
            if (views == null || views[type].isEmpty()) return null;
            return views[type].pop();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void addRecyclerView(View view, int type) {
        if (views[type] == null) {
            views[type] = new Stack<>();
        }
        views[type].push(view);
    }
}
