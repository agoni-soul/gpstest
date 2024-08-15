package com.soul.ui.textView

import android.content.Context
import android.graphics.Rect
import android.text.Layout
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import android.view.animation.Animation
import androidx.appcompat.widget.AppCompatTextView
import com.soul.ui.movementMethod.OverLinkMovementMethod


/**
 *     author : yangzy33
 *     time   : 2024-08-15
 *     desc   :
 *     version: 1.0
 */
class FoldTextView(context: Context, attributeSet: AttributeSet? = null, defStyleAttr: Int = 0):
    AppCompatTextView(context, attributeSet, defStyleAttr) {
    companion object {
        val ELLIPSIS_STRING = String(charArrayOf('\u2026'))
        val DEFAULT_MAX_LINE = 3
        val DEFAULT_OPEN_SUFFIX = " 展开"
        val DEFAULT_CLOSE_SUFFIX = " 收起"
    }

    var animating = false
    private var isClosed = false
    private var mMaxLines: Int = DEFAULT_MAX_LINE

    /**
     * TextView可展示宽度，包含paddingLeft和paddingRight
     */
    private var initWidth = 0

    /**
     * 原始文本
     */
    private var originalText: CharSequence? = null
    private var mCloseSpannableStr: SpannableStringBuilder? = null
    private var mOpenSpannableStr: SpannableStringBuilder? = null
    private var hasAnimation = false
    private var mOpenAnim : Animation? = null
    private var mCloseAnim: Animation? = null
    private var mOpenHeight: Int = 0
    private var mCLoseHeight: Int = 0
    private var mExpandable = false
    private var mCloseInNewLine = false
    private var mOpenSuffixSpan: SpannableString? = null
    private var mCloseSuffixSpan: SpannableString? = null

    private var mFoldMaxLine = 1

    constructor(context: Context): this(context, null, 0)
    constructor(context: Context, attributeSet: AttributeSet?):this(context, attributeSet, 0)

    init {
        movementMethod = OverLinkMovementMethod.getInstance()
        includeFontPadding = false
        updateOpenSuffixSpan()
        updateCloseSuffixSpan()
    }

    override fun hasOverlappingRendering(): Boolean = false

    fun setOriginalText(originalText: CharSequence?) {
        this.originalText = originalText
        mExpandable = false
        mCloseSpannableStr = SpannableStringBuilder()
        val maxLines = mMaxLines
        val tempText = charSequenceToSpannable(originalText)
        mOpenSpannableStr = charSequenceToSpannable(originalText)

        if (maxLines != -1) {
            val layout = createStaticLayout(tempText)
        }
    }

    private fun hasEnCharCount(str: CharSequence?): Int {
        str ?: return 0
        var count = 0
        if (str.isNotEmpty()) {
            str.forEach { c ->
                if (c in ' '..'~') {
                    count ++
                }
            }
        }
        return count
    }

    private fun switchOpenClose() {
        if (mExpandable) {
            isClosed = !isClosed
            if (isClosed) {
                close()
            } else {
                open()
            }
        }
    }

    fun setHasAnimation(hasAnimation: Boolean) {
        this.hasAnimation = hasAnimation
    }

    private fun open() {
        if (hasAnimation) {
            val layout = createStaticLayout(mOpenSpannableStr)
            mOpenHeight = layout.
        }
    }

    private fun createStaticLayout(spannable: SpannableStringBuilder): Layout {
        val contentWidth = initWidth - paddingLeft - paddingRight
    }

    private fun charSequenceToSpannable(originalText: CharSequence?): SpannableStringBuilder {
        return SpannableStringBuilder()
    }

    fun setFoldMaxLine(foldMaxLine: Int) {
        mFoldMaxLine = foldMaxLine
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val estimatedHeight = calculateNeededHeight(width)
        setMeasuredDimension(width, estimatedHeight)
    }

    private fun calculateNeededHeight(width: Int): Int {
        val paint = paint
        val text = text.toString()
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        val textHeight = bounds.height()

        var neededHeight = textHeight + paddingTop + paddingBottom
        if (lineCount > 0) {
            neededHeight *= lineCount
        }
        if (mFoldMaxLine < neededHeight) {
        }
        return neededHeight
    }
}