package com.zionhuang.music.utils.lyrics

import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint

data class LyricsEntry(
    val time: Long,
    val text: String,
) : Comparable<LyricsEntry> {
    var staticLayout: StaticLayout? = null
        private set
    var offset = Float.MIN_VALUE // distance to the top of [LyricsView]
    val height: Int
        get() = staticLayout?.height ?: 0

    fun init(paint: TextPaint, width: Int, gravity: Int) {
        staticLayout = StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(when (gravity) {
                GRAVITY_LEFT -> Layout.Alignment.ALIGN_NORMAL
                GRAVITY_CENTER -> Layout.Alignment.ALIGN_CENTER
                GRAVITY_RIGHT -> Layout.Alignment.ALIGN_OPPOSITE
                else -> Layout.Alignment.ALIGN_CENTER
            })
            .setLineSpacing(0f, 1f)
            .setIncludePad(false).build()
        offset = Float.MIN_VALUE
    }

    override fun compareTo(other: LyricsEntry): Int = (time - other.time).toInt()

    companion object {
        const val GRAVITY_LEFT = 0
        const val GRAVITY_CENTER = 1
        const val GRAVITY_RIGHT = 2
    }
}