package com.soul.ui.textView

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Build
import android.text.*
import android.text.style.AlignmentSpan
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
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
    private var mCloseHeight: Int = 0
    private var mExpandable = false
    private var mCloseInNewLine = false
    private var mOpenSuffixSpan: SpannableString? = null
    private var mCloseSuffixSpan: SpannableString? = null
    private var mOpenSuffixStr: String? = DEFAULT_OPEN_SUFFIX
    private var mCloseSuffixStr: String? = DEFAULT_CLOSE_SUFFIX
    private var mOpenSuffixColor = 0
    private var mCloseSuffixColor = 0
    private var mOnClickListener: OnClickListener? = null
    private var mCharSequenceToSpannableHandler: CharSequenceToSpannableHandler? = null

    private var mFoldMaxLine = 1

    constructor(context: Context): this(context, null, 0)
    constructor(context: Context, attributeSet: AttributeSet?):this(context, attributeSet, 0)

    init {
        mOpenSuffixColor = Color.parseColor("#F23030")
        mCloseSuffixColor = Color.parseColor("#F23030")
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
            mExpandable = layout.lineCount > maxLines
            if (mExpandable) {
                // 拼接展开内容
                if (mCloseInNewLine) {
                    mOpenSpannableStr?.append("\n")
                }
                if (mCloseSuffixSpan != null) {
                    mOpenSpannableStr?.append(mCloseSuffixSpan)
                }
                // 计算原文截取位置
                val endPos = layout.getLineEnd(maxLines - 1)
                mCloseSpannableStr = if ((originalText?.length ?: 0) <= endPos) {
                    charSequenceToSpannable(originalText)
                } else {
                    charSequenceToSpannable(originalText!!.subSequence(0, endPos))
                }
                var tempText2 = charSequenceToSpannable(mCloseSpannableStr).append(ELLIPSIS_STRING)
                if (mOpenSuffixSpan != null) {
                    tempText2.append(mOpenSuffixSpan)
                }
                // 循环判断，收起内容添加展开后缀后的内容
                var tempLayout = createStaticLayout(tempText2)
                while (tempLayout.lineCount > maxLines) {
                    val lastSpace = (mCloseSpannableStr?.length ?: 0) - 1
                    if (lastSpace == -1) {
                        break
                    }
                    mCloseSpannableStr = if ((originalText?.length ?: 0) <= lastSpace) {
                        charSequenceToSpannable(originalText)
                    } else {
                        charSequenceToSpannable(originalText!!.subSequence(0, lastSpace))
                    }
                    tempText2 = charSequenceToSpannable(mCloseSpannableStr).append(ELLIPSIS_STRING)
                    if (mOpenSuffixSpan != null) {
                        tempText2.append(mOpenSuffixSpan)
                    }
                    tempLayout = createStaticLayout(tempText2)
                }
                var lastSpace = (mCloseSpannableStr?.length ?: 0) - (mOpenSuffixSpan?.length ?: 0)
                if (lastSpace >= 0 && (originalText?.length ?: 0) > lastSpace) {
                    val redundantChar = originalText?.subSequence(lastSpace, lastSpace + (mOpenSuffixSpan?.length ?: 0))
                    val offset = hasEnCharCount(redundantChar) - hasEnCharCount(mOpenSuffixSpan) + 1
                    lastSpace = if (offset <= 0) lastSpace else lastSpace - offset
                    mCloseSpannableStr = charSequenceToSpannable(originalText?.subSequence(0, lastSpace))
                }
                mCloseHeight = tempLayout.height + paddingTop + paddingBottom
                mCloseSpannableStr?.append(ELLIPSIS_STRING)
                if (mOpenSuffixSpan != null) {
                    mCloseSpannableStr?.append(mOpenSuffixSpan)
                }
            }
        }
        isClosed = mExpandable
        if (mExpandable) {
            text = mCloseSpannableStr
            super.setOnClickListener { v ->
                switchOpenClose()
                mOnClickListener?.onClick(v)
            }
        } else {
            text = mOpenSpannableStr
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
            if (mOpenSpannableStr == null) return
            val layout = createStaticLayout(mOpenSpannableStr!!)
            mOpenHeight = layout.height + paddingTop + paddingBottom
            executeOpenAnim()
        } else {
            super@FoldTextView.setMaxLines(Int.MAX_VALUE)
            text = mOpenSpannableStr
            mOpenCloseCallback?.onOpen()
        }
    }

    private fun executeOpenAnim() {
        // 创建展开动画
        if (mOpenAnim == null) {
            mOpenAnim = ExpandCollapseAnimation(this, mCloseHeight, mOpenHeight)
            mOpenAnim!!.fillAfter = true
            mOpenAnim!!.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    super@FoldTextView.setMaxLines(Int.MAX_VALUE)
                    this@FoldTextView.text = mOpenSpannableStr
                }

                override fun onAnimationEnd(animation: Animation?) {
                    // 动画结束后textView设置展开的状态
                    this@FoldTextView.layoutParams.height = mOpenHeight
                    requestLayout()
                    animating = false
                }

                override fun onAnimationRepeat(animation: Animation?) {}

            })
        }
        if (animating) {
            return
        }
        animating = true
        clearAnimation()
        // 执行动画
        startAnimation(mOpenAnim)
    }

    private fun close() {
        if (hasAnimation) {
            executeCloseAnim()
        } else {
            super@FoldTextView.setMaxLines(mMaxLines)
            text = mCloseSpannableStr
            mOpenCloseCallback?.onClose()
        }
    }

    private fun executeCloseAnim() {
        // 创建收起动画
        if (mCloseAnim == null) {
            mCloseAnim = ExpandCollapseAnimation(this, mOpenHeight, mCloseHeight)
            mCloseAnim!!.fillAfter = true
            mCloseAnim!!.setAnimationListener(object: Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}

                override fun onAnimationEnd(animation: Animation?) {
                    animating = false
                    super@FoldTextView.setMaxLines(mMaxLines)
                    text = mCloseSpannableStr
                    this@FoldTextView.layoutParams.height = mCloseHeight
                    requestLayout()
                }

                override fun onAnimationRepeat(animation: Animation?) {}
            })
        }
        if (animating) return
        animating = true
        clearAnimation()
        startAnimation(mCloseAnim)
    }

    private fun createStaticLayout(spannable: SpannableStringBuilder): Layout {
        val contentWidth = initWidth - paddingLeft - paddingRight
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val builder = StaticLayout.Builder.obtain(spannable, 0, spannable.length, paint, contentWidth)
            builder.setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setIncludePad(includeFontPadding)
                .setLineSpacing(lineSpacingExtra, lineSpacingMultiplier)
            builder.build()
        } else
            StaticLayout(
                spannable, paint, contentWidth, Layout.Alignment.ALIGN_NORMAL,
                lineSpacingMultiplier, lineSpacingExtra, includeFontPadding
            )
    }

    private fun charSequenceToSpannable(charSequence: CharSequence?): SpannableStringBuilder {
        var spannableStringBuilder: SpannableStringBuilder? = null
        if (mCharSequenceToSpannableHandler != null && !charSequence.isNullOrEmpty()) {
            spannableStringBuilder = mCharSequenceToSpannableHandler?.charSequenceToSpannable(charSequence)
        }
        return spannableStringBuilder ?: SpannableStringBuilder(charSequence)
    }

    fun initWidth(width: Int) {
        initWidth = width
    }

    override fun setMaxLines(maxLines: Int) {
        mMaxLines = maxLines
        super.setMaxLines(maxLines)
    }

    fun setOpenSuffix(openSuffix: String?) {
        mOpenSuffixStr = openSuffix
        updateOpenSuffixSpan()
    }

    fun setOpenSuffixColor(openSuffixColor: Int) {
        mOpenSuffixColor = openSuffixColor
        updateOpenSuffixSpan()
    }

    private fun updateOpenSuffixSpan() {
        if (mOpenSuffixStr.isNullOrEmpty()) {
            mOpenSuffixSpan = null
            return
        }
        mOpenSuffixSpan = SpannableString(mOpenSuffixStr).apply {
            setSpan(StyleSpan(Typeface.BOLD),
                0,
                mOpenSuffixStr!!.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    switchOpenClose()
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = mOpenSuffixColor
                    ds.isUnderlineText = false
                }

            }, 0, mOpenSuffixStr!!.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    fun setCloseSuffix(closeSuffix: String) {
        mCloseSuffixStr = closeSuffix
        updateCloseSuffixSpan()
    }

    fun setCloseSuffixColor(closeSuffixColor: Int) {
        mCloseSuffixColor = closeSuffixColor
        updateCloseSuffixSpan()
    }

    fun setCloseInNewLine(closeInNewLine: Boolean) {
        mCloseInNewLine = closeInNewLine
        updateCloseSuffixSpan()
    }

    private fun updateCloseSuffixSpan() {
        if (mCloseSuffixStr.isNullOrEmpty()) {
            mCloseSuffixSpan = null
            return
        }
        mCloseSuffixSpan = SpannableString(mCloseSuffixStr)
        mCloseSuffixSpan!!.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            mCloseSuffixStr!!.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        if (mCloseInNewLine) {
            val alignmentSpan: AlignmentSpan =
                AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE)
            mCloseSuffixSpan!!.setSpan(alignmentSpan, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        mCloseSuffixSpan = SpannableString(mCloseSuffixStr).apply {
            if (mCloseInNewLine) {
                val alignmentSpan = AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE)
                setSpan(alignmentSpan, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    switchOpenClose()
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = mCloseSuffixColor
                    ds.isUnderlineText = false
                }
            }, 1, mCloseSuffixStr!!.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        mOnClickListener = l
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

    var mOpenCloseCallback: OpenAndCloseCallback? = null
        private set

    fun setOpenAndCloseCallback(callback: OpenAndCloseCallback?) {
        mOpenCloseCallback = callback
    }

    fun setCharSequenceToSpannableHandler(handler: CharSequenceToSpannableHandler?) {
        mCharSequenceToSpannableHandler = handler
    }

    interface OpenAndCloseCallback {
        fun onOpen()
        fun onClose()
    }

    interface CharSequenceToSpannableHandler {
        fun charSequenceToSpannable(charSequence: CharSequence): SpannableStringBuilder
    }

    inner class ExpandCollapseAnimation(target: View, startHeight: Int, endHeight: Int): Animation() {
        private val mTargetView: View = target
        private val mStartHeight = startHeight
        private val mEndHeight = endHeight

        init {
            duration = 400
        }

        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            mTargetView.scaleY = 0F
            mTargetView.layoutParams.height = ((mEndHeight - mStartHeight) * interpolatedTime + mStartHeight).toInt()
            mTargetView.requestLayout()
        }
    }
}