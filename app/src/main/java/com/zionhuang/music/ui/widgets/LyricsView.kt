package com.zionhuang.music.ui.widgets

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Looper
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.format.DateUtils.SECOND_IN_MILLIS
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.widget.Scroller
import androidx.core.content.ContextCompat
import androidx.core.graphics.withSave
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.zionhuang.music.R
import com.zionhuang.music.utils.lyrics.LyricsEntry
import com.zionhuang.music.utils.lyrics.LyricsUtils
import kotlinx.coroutines.*
import java.lang.Runnable
import kotlin.math.abs

/**
 * Modified from [RetroMusicPlayer](https://github.com/RetroMusicPlayer/RetroMusicPlayer)
 */
@SuppressLint("StaticFieldLeak")
class LyricsView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    private val lrcEntryList: MutableList<LyricsEntry> = ArrayList()
    private val lrcPaint = TextPaint()
    private val timePaint = TextPaint()
    private lateinit var timeFontMetrics: Paint.FontMetrics
    private var dividerHeight = 0f
    private var animationDuration: Long = 0
    private var unsyncedTextColor = 0
    private var normalTextColor = 0
    private var normalTextSize = 0f
    private var currentTextColor = 0
    private var currentTextSize = 0f
    private var animatedCurrentTextSize = 0f
    private var isShowTimeline = false
    private var timelineTextColor = 0
    private var timelineColor = 0
    private var timeTextColor = 0
    private var drawableWidth = 0
    private var timeTextWidth = 0
    private var defaultLabel: String = ""
    private var showLabel: Boolean = false
    private var lrcPadding = 0f
    private var onLyricsClickListener: OnLyricsClickListener? = null
    private var animator: ValueAnimator? = null
    private lateinit var gestureDetector: GestureDetector
    private lateinit var scroller: Scroller
    private var offset = 0f
    private var previousLine = -1
    private var currentLine = 0
    private var inActive = false
    private var isTouching = false
    private var isFling = false
    private var textGravity = 0 // left/center/right
    private var isSyncedLyrics = true
    var immersivePaddingTop = 0
    var isPlaying = false
        set(value) {
            if (field == value) return
            field = value
            if (value && !isFling && !inActive) {
                smoothScrollTo(currentLine)
            }
        }
    private val hideTimelineRunnable = Runnable {
        inActive = false
        if (hasLyrics() && isSyncedLyrics && isPlaying) {
            smoothScrollTo(currentLine)
        }
    }

    private val viewScope = CoroutineScope(Dispatchers.Main + Job())

    private val simpleOnGestureListener = object : SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            if (hasLyrics() && onLyricsClickListener != null) {
                if (offset != getOffset(0)) {
                    parent.requestDisallowInterceptTouchEvent(true)
                }
                scroller.forceFinished(true)
                removeCallbacks(hideTimelineRunnable)
                isTouching = true
                inActive = true
                invalidate()
                return true
            }
            return super.onDown(e)
        }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            if (offset == getOffset(0) && distanceY < 0F) {
                return super.onScroll(e1, e2, distanceX, distanceY)
            }
            if (hasLyrics()) {
                offset = (offset - distanceY).coerceIn(getOffset(lrcEntryList.size - 1), getOffset(0))
                invalidate()
                parent.requestDisallowInterceptTouchEvent(true)
                return true
            }
            return super.onScroll(e1, e2, distanceX, distanceY)
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            if (hasLyrics()) {
                scroller.fling(0, offset.toInt(), 0, velocityY.toInt(), 0, 0, getOffset(lrcEntryList.size - 1).toInt(), getOffset(0).toInt())
                isFling = true
                return true
            }
            return super.onFling(e1, e2, velocityX, velocityY)
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            if (hasLyrics() && isSyncedLyrics) {
                val line = getLineByOffset(offset + height / 2 - e.y)
                val lineTime = lrcEntryList[line].time
                if (onLyricsClickListener?.onLyricsClick(lineTime) == true) {
                    inActive = false
                    removeCallbacks(hideTimelineRunnable)
//                    previousLine = if (line != currentLine) line else -1
//                    currentLine = line
//                    updateCurrentTextSize()
//                    smoothScrollTo(line)
                    return true
                }
            } else {
                callOnClick()
                return true
            }
            return super.onSingleTapConfirmed(e)
        }
    }

    private fun init(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.LyricsView)
        currentTextSize = typedArray.getDimension(R.styleable.LyricsView_lrcTextSize, resources.getDimension(R.dimen.lrc_text_size))
        animatedCurrentTextSize = currentTextSize
        normalTextSize = typedArray.getDimension(R.styleable.LyricsView_lrcNormalTextSize, resources.getDimension(R.dimen.lrc_text_size)).takeIf { it != 0f } ?: currentTextSize
        dividerHeight = typedArray.getDimension(R.styleable.LyricsView_lrcDividerHeight, resources.getDimension(R.dimen.lrc_divider_height))
        val defaultDuration = resources.getInteger(R.integer.lrc_animation_duration)
        animationDuration = typedArray.getInt(R.styleable.LyricsView_lrcAnimationDuration, defaultDuration).toLong().takeIf { it > 0 } ?: defaultDuration.toLong()
        unsyncedTextColor = typedArray.getColor(R.styleable.LyricsView_lrcUnsyncedTextColor, ContextCompat.getColor(context, R.color.lrc_unsynced_text_color))
        normalTextColor = typedArray.getColor(R.styleable.LyricsView_lrcNormalTextColor, ContextCompat.getColor(context, R.color.lrc_normal_text_color))
        currentTextColor = typedArray.getColor(R.styleable.LyricsView_lrcCurrentTextColor, ContextCompat.getColor(context, R.color.lrc_current_text_color))
        isShowTimeline = typedArray.getBoolean(R.styleable.LyricsView_lrcShowTimeline, false)
        timelineTextColor = typedArray.getColor(R.styleable.LyricsView_lrcTimelineTextColor, ContextCompat.getColor(context, R.color.lrc_timeline_text_color))
        defaultLabel = typedArray.getString(R.styleable.LyricsView_lrcLabel).takeUnless { it.isNullOrEmpty() } ?: "Empty"
        lrcPadding = typedArray.getDimension(R.styleable.LyricsView_lrcPadding, 0f)
        timelineColor = typedArray.getColor(R.styleable.LyricsView_lrcTimelineColor, ContextCompat.getColor(context, R.color.lrc_timeline_color))
        val timelineHeight = typedArray.getDimension(R.styleable.LyricsView_lrcTimelineHeight, resources.getDimension(R.dimen.lrc_timeline_height))
        timeTextColor = typedArray.getColor(R.styleable.LyricsView_lrcTimeTextColor, ContextCompat.getColor(context, R.color.lrc_time_text_color))
        val timeTextSize = typedArray.getDimension(R.styleable.LyricsView_lrcTimeTextSize, resources.getDimension(R.dimen.lrc_time_text_size))
        textGravity = typedArray.getInteger(R.styleable.LyricsView_lrcTextGravity, LyricsEntry.GRAVITY_CENTER)
        typedArray.recycle()
        drawableWidth = resources.getDimension(R.dimen.lrc_drawable_width).toInt()
        timeTextWidth = resources.getDimension(R.dimen.lrc_time_width).toInt()
        lrcPaint.apply {
            isAntiAlias = true
            textSize = currentTextSize
            textAlign = Paint.Align.LEFT
        }
        timePaint.apply {
            isAntiAlias = true
            textSize = timeTextSize
            textAlign = Paint.Align.CENTER
            strokeWidth = timelineHeight
            strokeCap = Paint.Cap.ROUND
        }
        timeFontMetrics = timePaint.fontMetrics
        gestureDetector = GestureDetector(context, simpleOnGestureListener).apply {
            setIsLongpressEnabled(false)
        }
        scroller = Scroller(context)
    }

    fun setNormalColor(normalColor: Int) {
        normalTextColor = normalColor
        postInvalidate()
    }

    fun setCurrentColor(currentColor: Int) {
        currentTextColor = currentColor
        postInvalidate()
    }

    fun setTimelineTextColor(timelineTextColor: Int) {
        this.timelineTextColor = timelineTextColor
        postInvalidate()
    }

    fun setTimelineColor(timelineColor: Int) {
        this.timelineColor = timelineColor
        postInvalidate()
    }

    fun setTimeTextColor(timeTextColor: Int) {
        this.timeTextColor = timeTextColor
        postInvalidate()
    }

    fun setDraggable(draggable: Boolean, onPlayClickListener: OnLyricsClickListener?) {
        this.onLyricsClickListener = if (draggable) {
            requireNotNull(onPlayClickListener) { "if draggable == true, onPlayClickListener must not be null" }
            onPlayClickListener
        } else {
            null
        }
    }

    fun setLabel(label: String) {
        runOnUi {
            defaultLabel = label
            invalidate()
        }
    }

    fun setTextGravity(gravity: Int) {
        if (textGravity == gravity) {
            return
        }
        textGravity = gravity
        lrcEntryList.forEach { lrcEntry ->
            lrcEntry.init(lrcPaint, lrcWidth.toInt(), textGravity)
        }
        postInvalidate()
    }

    fun loadLyrics(lyrics: String) {
        runOnUi {
            reset()
            viewScope.launch(Dispatchers.IO) {
                val entries = if (lyrics.startsWith("[")) { // synced
                    listOf(LyricsEntry(0L, "")) + LyricsUtils.parseLyrics(lyrics)
                } else { // unsynced
                    lyrics.lines().mapIndexed { index, line -> LyricsEntry(index * 100L, line) }
                }
                isSyncedLyrics = lyrics.startsWith("[")
                withContext(Dispatchers.Main) {
                    onLyricsLoaded(entries)
                }
            }
        }
    }

    fun hasLyrics(): Boolean = lrcEntryList.isNotEmpty()

    fun updateTime(time: Long, animate: Boolean = true) {
        runOnUi {
            if (!hasLyrics() || !isSyncedLyrics) {
                return@runOnUi
            }
            val line = findShowLine(time + animationDuration)
            if (line != currentLine) {
                previousLine = currentLine
                currentLine = line
                updateCurrentTextSize(animate)
                if (!animate) {
                    scroller.forceFinished(true)
                }
                if ((!isFling && !inActive) || !animate) { // !animate means the user is dragging the seekbar
                    smoothScrollTo(line, if (animate) animationDuration else 0)
                }
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            initEntryList()
            if (hasLyrics()) {
                smoothScrollTo(currentLine, 0L)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerY = height / 2

        if (!hasLyrics() && showLabel) {
            lrcPaint.color = currentTextColor
            val staticLayout = StaticLayout.Builder.obtain(defaultLabel, 0, defaultLabel.length, lrcPaint, lrcWidth.toInt())
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(0f, 1f)
                .setIncludePad(false).build()
            drawText(canvas, staticLayout, centerY.toFloat())
            return
        }
        val centerLine = getCenterLine()
        if (inActive && isShowTimeline) {
            timePaint.color = timeTextColor
            val timeText = LyricsUtils.formatTime(lrcEntryList[centerLine].time)
            val timeX = (width - timeTextWidth / 2).toFloat()
            val timeY = centerY - (timeFontMetrics.descent + timeFontMetrics.ascent) / 2
            canvas.drawText(timeText, timeX, timeY, timePaint)
        }
        canvas.translate(0f, offset)
        var y = 0f
        for (i in lrcEntryList.indices) {
            if (i > 0) {
                y += (lrcEntryList[i - 1].height + lrcEntryList[i].height) / 2 + dividerHeight
            }
            if (!isSyncedLyrics) {
                lrcPaint.textSize = normalTextSize
                lrcPaint.color = unsyncedTextColor
            } else if (i == currentLine) {
                lrcPaint.textSize = animatedCurrentTextSize
                lrcPaint.color = currentTextColor
            } else {
                lrcPaint.textSize = if (i != previousLine) normalTextSize else currentTextSize - (animatedCurrentTextSize - normalTextSize)
                lrcPaint.color = normalTextColor
            }
            drawText(canvas, lrcEntryList[i].staticLayout!!, y)
        }
    }

    private fun drawText(canvas: Canvas, staticLayout: StaticLayout, y: Float) {
        canvas.withSave {
            translate(lrcPadding, y - staticLayout.height / 2)
            staticLayout.draw(this)
        }
    }

    private fun updateCurrentTextSize(animate: Boolean = true) {
        if (animate) {
            ValueAnimator.ofFloat(normalTextSize, currentTextSize).apply {
                interpolator = FastOutSlowInInterpolator()
                duration = animationDuration
                addUpdateListener {
                    animatedCurrentTextSize = it.animatedValue as Float
                    invalidate()
                }
                start()
            }
        } else {
            animatedCurrentTextSize = currentTextSize
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            isTouching = false
            if (hasLyrics() && isSyncedLyrics && !isFling) {
                postDelayed(hideTimelineRunnable, TIMELINE_KEEP_TIME)
            }
        }
        return gestureDetector.onTouchEvent(event)
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            offset = scroller.currY.toFloat()
            invalidate()
        }
        if (isFling && scroller.isFinished) {
            isFling = false
            if (hasLyrics() && isSyncedLyrics && !isTouching) {
                postDelayed(hideTimelineRunnable, TIMELINE_KEEP_TIME)
            }
        }
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(hideTimelineRunnable)
        viewScope.cancel()
        super.onDetachedFromWindow()
    }

    private fun onLyricsLoaded(entryList: List<LyricsEntry>?) {
        if (entryList != null && entryList.isNotEmpty()) {
            lrcEntryList.addAll(entryList)
        }
        lrcEntryList.sort()
        initEntryList()
        invalidate()
    }

    private fun initEntryList() {
        if (!hasLyrics() || width == 0) return
        lrcEntryList.forEach { lrcEntry ->
            lrcEntry.init(lrcPaint, lrcWidth.toInt(), textGravity)
        }
        offset = (height / 2).toFloat()
    }

    fun reset() {
        endAnimation()
        scroller.forceFinished(true)
        inActive = false
        isTouching = false
        isFling = false
        removeCallbacks(hideTimelineRunnable)
        lrcEntryList.clear()
        offset = 0f
        previousLine = -1
        currentLine = 0
        invalidate()
    }

    private fun smoothScrollTo(line: Int, duration: Long = animationDuration) {
        val endOffset = getOffset(line)
        endAnimation()
        animator = ValueAnimator.ofFloat(offset, endOffset).apply {
            this.duration = duration
            interpolator = FastOutSlowInInterpolator()
            addUpdateListener { valueAnimator ->
                offset = valueAnimator.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun endAnimation() {
        if (animator != null && animator!!.isRunning) {
            animator!!.end()
        }
    }

    private fun findShowLine(time: Long): Int {
        var left = 0
        var right = lrcEntryList.size
        while (left <= right) {
            val middle = (left + right) / 2
            val middleTime = lrcEntryList[middle].time
            if (time < middleTime) {
                right = middle - 1
            } else {
                if (middle + 1 >= lrcEntryList.size || time < lrcEntryList[middle + 1].time) {
                    return middle
                }
                left = middle + 1
            }
        }
        return 0
    }

    private fun getCenterLine() = getLineByOffset(offset)
    private fun getLineByOffset(offset: Float): Int {
        var line = 0
        var minDistance = Float.MAX_VALUE
        for (i in lrcEntryList.indices) {
            if (abs(offset + immersivePaddingTop - getOffset(i)) < minDistance) {
                minDistance = abs(offset + immersivePaddingTop - getOffset(i))
                line = i
            }
        }
        return line
    }

    private fun getOffset(line: Int): Float {
        if (lrcEntryList.isEmpty()) return 0F
        if (lrcEntryList[line].offset == Float.MIN_VALUE) {
            var offset = ((height + immersivePaddingTop) / 2).toFloat()
            for (i in 1..line) {
                offset -= (lrcEntryList[i - 1].height + lrcEntryList[i].height) / 2 + dividerHeight
            }
            lrcEntryList[line].offset = offset
        }
        return lrcEntryList[line].offset
    }

    private val lrcWidth: Float
        get() = width - lrcPadding * 2

    private fun runOnUi(r: Runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            r.run()
        } else {
            post(r)
        }
    }

    fun interface OnLyricsClickListener {
        fun onLyricsClick(time: Long): Boolean
    }

    companion object {
        private const val TIMELINE_KEEP_TIME = 4 * SECOND_IN_MILLIS
    }

    init {
        init(attrs)
    }
}