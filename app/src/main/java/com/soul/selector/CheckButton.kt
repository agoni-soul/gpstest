package com.soul.selector

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.Checkable
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.res.ResourcesCompat
import com.soul.gpstest.R

/**
 *
 * @author : haha
 * @date   : 2024-10-11
 * @desc   : button选中按钮
 *
 */
class CheckButton(context: Context, attr: AttributeSet? = null, defStyleAttr: Int = 0): AppCompatButton(context, attr, defStyleAttr), Checkable {
    private var mChecked: Boolean = false
    private var mButtonDrawable: Drawable? = null

    private var mCheckedDrawableId: Int = 0
    private var mDefaultDrawableId: Int = 0
    private var mPressDrawableId: Int = 0

    constructor(context: Context): this(context, null, 0)

    constructor(context: Context, attr: AttributeSet?): this(context, attr, 0)

    override fun setChecked(checked: Boolean) {
        if (mChecked != checked) {
            mChecked = checked
            refreshDrawableState()
        }
    }

    /**
//     * Sets a drawable as the compound button image.
//     *
//     * @param drawable the drawable to set
//     * @attr ref android.R.styleable#CompoundButton_button
//     */
//    @RequiresApi(Build.VERSION_CODES.M)
//    fun setButtonDrawable(drawable: Drawable?) {
//        if (mButtonDrawable !== drawable) {
//            if (mButtonDrawable != null) {
//                mButtonDrawable!!.callback = null
//                unscheduleDrawable(mButtonDrawable)
//            }
//
//            mButtonDrawable = drawable
//
//            if (drawable != null) {
//                drawable.callback = this
//                drawable.setLayoutDirection(layoutDirection)
//                if (drawable.isStateful) {
//                    drawable.setState(drawableState)
//                }
//                drawable.setVisible(visibility == VISIBLE, false)
//                minHeight = drawable.intrinsicHeight
//                applyButtonTint()
//            }
//        }
//    }
//
//    private fun applyButtonTint() {
//        if (mButtonDrawable != null) {
//            mButtonDrawable = mButtonDrawable!!.mutate()
//            // The drawable (or one of its children) may not have been
//            // stateful before applying the tint, so let's try again.
//            if (mButtonDrawable!!.isStateful) {
//                mButtonDrawable!!.setState(drawableState)
//            }
//        }
//    }
//
//    fun getButtonDrawable(): Drawable? {
//        return mButtonDrawable
//    }
//
//    override fun drawableStateChanged() {
//        super.drawableStateChanged()
//        val buttonDrawable: Drawable? = mButtonDrawable
//        if (buttonDrawable != null && buttonDrawable.isStateful
//            && buttonDrawable.setState(drawableState)
//        ) {
//            invalidateDrawable(buttonDrawable)
//        }
//    }
//
//    override fun onDraw(canvas: Canvas?) {
//        val buttonDrawable = mButtonDrawable
//        if (buttonDrawable != null) {
//            val verticalGravity = gravity and Gravity.VERTICAL_GRAVITY_MASK
//            val drawableHeight = buttonDrawable.intrinsicHeight
//            val drawableWidth = buttonDrawable.intrinsicWidth
//            val top = when (verticalGravity) {
//                Gravity.BOTTOM -> height - drawableHeight
//                Gravity.CENTER_VERTICAL -> (height - drawableHeight) / 2
//                else -> 0
//            }
//            val bottom = top + drawableHeight
//            val left = width - drawableWidth
//            val right = width
//
//            buttonDrawable.setBounds(left, top, right, bottom)
//
//            val background = background
//            background?.setHotspotBounds(left, top, right, bottom)
//        }
//
//        super.onDraw(canvas)
//
//        if (buttonDrawable != null) {
//            val scrollX: Int = scrollX
//            val scrollY: Int = scrollY
//            if (scrollX == 0 && scrollY == 0 && canvas != null) {
//                buttonDrawable.draw(canvas)
//            } else {
//                canvas!!.translate(scrollX.toFloat(), scrollY.toFloat())
//                buttonDrawable.draw(canvas)
//                canvas.translate(-scrollX.toFloat(), -scrollY.toFloat())
//            }
//        }
//    }

    fun setCheckedDrawable(@DrawableRes id: Int) {
        mCheckedDrawableId = id
    }

    fun setDefaultDrawable(@DrawableRes id: Int) {
        mDefaultDrawableId = id
    }

    fun setPressDrawable(@DrawableRes id: Int) {
        mPressDrawableId = id
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            if (event.x < 0 || event.y < 0 || event.x > width || event.y > height) {
                if (mChecked) {
                    setInternalDrawable(mCheckedDrawableId)
                } else {
                    setInternalDrawable(mDefaultDrawableId)
                }
            } else {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        setInternalDrawable(mPressDrawableId)
                    }
                    MotionEvent.ACTION_UP -> {
                        if (isChecked) {
                            setInternalDrawable(mCheckedDrawableId)
                        } else {
                            setInternalDrawable(mDefaultDrawableId)
                        }
                    }
                    MotionEvent.ACTION_MOVE -> {
                        setInternalDrawable(mPressDrawableId)
                    }
                    else -> {
                        setInternalDrawable(mDefaultDrawableId)
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun setInternalDrawable(@DrawableRes id: Int) {
        if (id != 0) {
            background = ResourcesCompat.getDrawable(resources, id, null)
        }
    }

    override fun isChecked(): Boolean {
        return mChecked
    }

    override fun toggle() {
        isChecked = !mChecked
    }

    override fun getAccessibilityClassName(): CharSequence {
        return this.javaClass.name
    }
}

fun CheckButton.setDefaultAllDrawables() {
    setPressDrawable(R.drawable.drawable_selector_bg_press)
    setCheckedDrawable(R.drawable.drawable_shape_checkbox_select)
    setDefaultDrawable(R.drawable.drawable_shape_checkbox_null)
}