package com.soul.slidedelete

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewGroup
import android.widget.Scroller
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs


/**
 *     author : yangzy33
 *     time   : 2024-05-22
 *     desc   :
 *     version: 1.0
 */
class SlideRecyclerView(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0): RecyclerView(context, attrs, defStyleAttr) {
    private val TAG = javaClass.simpleName
    companion object {
        // 触摸到的点不在子View范围内
        private val INVALID_POSITION = -1
        // 子ItemView不含两个子View
        private val INVALID_CHILD_WIDTH = -1
        // 最小滑动速度
        private val SNAP_VELOCITY = 600
    }

    // 速度追踪器
    private var mVelocityTracker: VelocityTracker? = null
    // 认为是滑动的最小距离（一般由系统提供）
    private var mTouchSlop: Int = 0
    // 子View所在的矩形范围
    private var mTouchFrame: Rect? = null
    private var mScroller: Scroller? = null
    // 滑动过程中记录上次触碰点
    private var mLastX = 0f
    // 首次触碰范围
    private var mFirstX = 0f
    private var mFirstY = 0f
    // 是否滑动子View
    private var mIsSlide: Boolean = false
    // 触碰的子View
    private var mFlingView: ViewGroup? = null
    // 触碰的view的位置
    private var mPosition = 0
    // 菜单按钮宽
    private var mMenuViewWidth = 0

    init {
        mScroller = Scroller(context)
    }

    constructor(context: Context): this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

    override fun onInterceptTouchEvent(e: MotionEvent?): Boolean {
        if (e == null) return super.onInterceptTouchEvent(e)
        val x = e.x.toInt()
        val y = e.y.toInt()
        obtainVelocity(e)
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                // 如果动画还没停止，则立即终止动画
                if (mScroller?.isFinished == false) {
                    mScroller!!.abortAnimation()
                }
                mFirstX = x.toFloat()
                mLastX = x.toFloat()
                mFirstY = y.toFloat()
                if (mPosition != INVALID_POSITION) {
                    val view = mFlingView
                    // 获取触碰点所在的view
                    mFlingView = getChildAt(mPosition - (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()) as ViewGroup
                    // 这里判断一下如果之前触碰的view已经打开，而当前碰到的view不是那个view则立即关闭之前的view，此处并不需要担动画没完成冲突，因为之前已经abortAnimation
                    if (view != null && mFlingView != view && view.scrollX != 0) {
                        view.scrollTo(0, 0)
                    }
                    // 这里进行了强制的要求，RecyclerView的子ViewGroup必须要有2个子view,这样菜单按钮才会有值，
                    // 需要注意的是:如果不定制RecyclerView的子View，则要求子View必须要有固定的width。
                    // 比如使用LinearLayout作为根布局，而content部分width已经是match_parent，此时如果菜单view用的是wrap_content，menu的宽度就会为0。
                    if (mFlingView?.getChildCount() == 2) {
                        mMenuViewWidth = mFlingView!!.getChildAt(1).width
                    } else {
                        mMenuViewWidth = INVALID_CHILD_WIDTH
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                mVelocityTracker?.computeCurrentVelocity(1000);
                // 此处有俩判断，满足其一则认为是侧滑：
                // 1.如果x方向速度大于y方向速度，且大于最小速度限制；
                // 2.如果x方向的侧滑距离大于y方向滑动距离，且x方向达到最小滑动距离；
                val xVelocity = mVelocityTracker?.xVelocity ?: 0f
                val yVelocity = mVelocityTracker?.yVelocity ?: 0f
                if (abs(xVelocity) > SNAP_VELOCITY && abs(xVelocity) > abs(yVelocity)
                    || abs(x - mFirstX) >= mTouchSlop
                    && abs(x - mFirstX) > abs(y - mFirstY)
                ) {
                    mIsSlide = true
                    return true
                }
            }
            MotionEvent.ACTION_UP -> {
                releaseVelocity()
            }
        }
        return super.onInterceptTouchEvent(e)
    }

    private fun obtainVelocity(event: MotionEvent) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
        mVelocityTracker!!.addMovement(event)
    }

    private fun releaseVelocity() {
        if (mVelocityTracker != null) {
            mVelocityTracker!!.clear()
            mVelocityTracker!!.recycle()
            mVelocityTracker = null
        }
    }

    override fun onTouchEvent(e: MotionEvent?): Boolean {
        return super.onTouchEvent(e)
    }
}