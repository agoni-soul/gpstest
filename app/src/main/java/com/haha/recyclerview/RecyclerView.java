package com.haha.recyclerview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import com.haha.hahalearn.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: haha
 * @date: 2025/4/11
 * Description: RecyclerView自定义
 **/
public class RecyclerView extends ViewGroup {
    // 用来判断onLayout中的逻辑是否执行
    private boolean needRelayout;
    // 一屏幕的View集合
    private Adapter adapter;
    private List<View> viewList;
    private Recycler recycler;
    // 每一行的高度
    private int[] heights;
    private int rowCount;

    private int width;
    private int height;

    // 最小滑动距离
    private int touchSlop;
    // 当前滑动的y值
    private int curY;
    // 偏移距离(第一个可见item的左上顶点距离屏幕左上角的距离)
    // 前一点(item左上角最开始的位置) - 后一个点(滑动后item左上角的位置)
    private int scrollY;
    // 滑动第几行(表示屏幕上第一个可见item在实际数据中的索引)
    private int firstRow;

    public RecyclerView(Context context) {
        super(context);
    }

    public RecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        needRelayout = true;
        viewList = new ArrayList<>();
        // 获取最小滑动距离
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        touchSlop = viewConfiguration.getScaledTouchSlop();
    }

    public void setAdapter(Adapter adapter) {
        if (adapter != null) {
            needRelayout = true;
            viewList = new ArrayList<>();
            this.adapter = adapter;
            recycler = new Recycler(adapter.getCount());
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercept = false;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                curY = (int) ev.getRawY();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                // 获取滑动的距离
                int moveY = Math.abs(curY - (int) ev.getRawY());
                if (moveY > touchSlop) {
                    // 如果大于最小滑动距离，则认为发生了滑动
                    intercept = true;
                }
            }
        }
        return intercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE: {
                int y = (int) event.getRawY();
                int diffY = curY - y;
                // 滑动，直接调用滑动的是一块固定的画布，所以要重写scrollBy
                scrollBy(0, diffY);
                break;
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * 需要重写scrollBy，因为默认的scrollBy滑动的是一块固定的画布
     * 我们需要的是不断地去更新item的位置，重新去进行摆放
     *
     * @param x the amount of pixels to scroll by horizontally
     * @param y the amount of pixels to scroll by vertically
     */
    @Override
    public void scrollBy(int x, int y) {
        scrollY += y;

        // 修正
        scrollY = scrollBounds(scrollY, firstRow, heights, height);
        if (scrollY > 0) {
            // 向上划
            while (heights[firstRow] < scrollY) {
                // 如果滑动的距离大于第一个可见item的高度
                if (!viewList.isEmpty()) {
                    // 移除最顶上的view
                    removeView(viewList.remove(0));
                }
                scrollY -= heights[firstRow];
                firstRow++;
            }

            while (getFilledHeight() < height) {
                // 如果当前填充屏幕的item总高度小于rv高度
                // 开始添加view
                int size = viewList.size();
                int dataIndex = firstRow + size;
                View view = obtain(dataIndex, width, heights[dataIndex]);
                viewList.add(view);
            }
        } else {
            // 向下滑
            while (!viewList.isEmpty() && getFilledHeight() - heights[firstRow + viewList.size() - 1] < scrollY) {
                View view = viewList.remove(viewList.size() - 1);
                removeView(view);
            }
            while (scrollY < 0) {
                firstRow--;
                View view = obtain(firstRow, width, heights[firstRow]);
                viewList.add(0, view);
                scrollY += heights[firstRow + 1];
            }
        }
        repositionViews();
    }

    private int scrollBounds(int scrollY, int firstRow, int[] heights, int viewSize) {
        if (scrollY < 0) {
            // 修整下滑临界值
            scrollY = Math.max(scrollY, -sumArray(heights, 0, firstRow));
        } else if (scrollY > 0) {
            scrollY = Math.min(scrollY, Math.max(0, sumArray(heights, firstRow, heights.length - viewSize)));
        }
        return scrollY;
    }

    private int sumArray(int[] heights, int firstIndex, int count) {
        int sum = 0;
        count += firstIndex;
        for (int i = firstIndex; i < count; i++) {
            sum += heights[i];
        }
        return sum;
    }

    private int getFilledHeight() {
        return sumArray(heights, firstRow, viewList.size()) - scrollY;
    }

    private void repositionViews() {
        int top = -scrollY, bottom;
        int i = firstRow;
        for (View view : viewList) {
            bottom = top + heights[i];
            view.layout(0, top, width, bottom);
            top = bottom;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (needRelayout || changed) {
            // 测量
            needRelayout = false;

            // 摆放的时候，初始化
            viewList.clear();
            // 比较耗时
            removeAllViews();
            if (adapter != null) {
                rowCount = adapter.getCount();
                heights = new int[rowCount];
                // 高度
                for (int i = 0; i < rowCount; i++) {
                    // 依赖这个方法测量item的高度
                    heights[i] += adapter.getHeight(i);
                }

                width = r - l;
                height = b - t;
                int top = 0;
                int bottom = 0;

                for (int i = 0; i < rowCount && top < height; i++) {
                    bottom = top + heights[i];
                    // 实例化 布局
                    // 怎么摆放
                    // 摆放多少个
                    View view = makeAndSetup(i, 0, top, width, bottom);
                    viewList.add(view);
                    top = bottom;
                }
            }
        }
    }

    private View makeAndSetup(int indexData, int left, int top, int right, int bottom) {
        View view = obtain(indexData, right - left, bottom - top);
        // 通过obtain()方法获取的view是没有宽高的，需要调用layout方法进行摆放
        view.layout(left, top, right, bottom);
        return view;
    }

    private View obtain(int row, int width, int height) {
        int type = adapter.getItemViewType(row);
        View recyclerView = recycler.getRecyclerView(type);
        View view;
        // 回收池里面取不出来
        if (recyclerView == null) {
            view = adapter.onCreateViewHolder(row, null, this);
            if (view == null) {
                throw new RuntimeException("onCreateViewHolder 必须初始化");
            }
        } else {
            // 如果从回收池里拿到了可以复用的item，则刷新该item上的数据
            view = adapter.onBinderViewHolder(row, recyclerView, this);
        }
        if (view == null) {
            throw new RuntimeException("convertView is not null");
        }
        // tag值，填充，移除
        // 因为回收池的填充和移除需要成对出现，所以需要给item设置TAG
        // 当回收池中有view被移除时，就会有屏幕上划出去的view填充进去
        view.setTag(R.id.tag_type_view, type);
        // 测量
        view.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        addView(view, 0);
        return view;
    }

    @Override
    public void removeView(View view) {
        super.removeView(view);
        int type = (int) view.getTag(R.id.tag_type_view);
        recycler.addRecyclerView(view, type);
    }

    interface Adapter {
        // 通用方法
        View onCreateViewHolder(int position, View contentView, ViewGroup parent);

        View onBinderViewHolder(int position, View contentView, ViewGroup parent);

        // Item类型
        int getItemViewType(int row);

        int getViewTypeCount();

        int getCount();

        // 额外加
        int getHeight(int index);

    }
}
