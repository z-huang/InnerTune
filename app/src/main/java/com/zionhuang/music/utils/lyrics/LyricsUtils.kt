package com.zionhuang.music.utils.lyrics

import android.text.format.DateUtils.MINUTE_IN_MILLIS
import android.text.format.DateUtils.SECOND_IN_MILLIS
import java.util.*

object LyricsUtils {
    private val LINE_REGEX = "((\\[\\d\\d:\\d\\d\\.\\d{2,3}\\])+)(.+)".toRegex()
    private val TIME_REGEX = "\\[(\\d\\d):(\\d\\d)\\.(\\d{2,3})\\]".toRegex()

    fun parseLyrics(lyrics: String): List<LyricsEntry> =
        lyrics.lines()
            .flatMap { line ->
                parseLine(line).orEmpty()
            }.sorted()

    private fun parseLine(line: String): List<LyricsEntry>? {
        if (line.isEmpty()) {
            return null
        }
        val matchResult = LINE_REGEX.matchEntire(line.trim()) ?: return null
        val times = matchResult.groupValues[1]
        val text = matchResult.groupValues[3]
        val timeMatchResults = TIME_REGEX.findAll(times)

        return timeMatchResults.map { timeMatchResult ->
            val min = timeMatchResult.groupValues[1].toLong()
            val sec = timeMatchResult.groupValues[2].toLong()
            val milString = timeMatchResult.groupValues[3]
            var mil = milString.toLong()
            if (milString.length == 2) {
                mil *= 10
            }
            val time = min * MINUTE_IN_MILLIS + sec * SECOND_IN_MILLIS + mil
            LyricsEntry(time, text)
        }.toList()
    }

    fun formatTime(milli: Long): String {
        val m = (milli / MINUTE_IN_MILLIS).toInt()
        val s = (milli / SECOND_IN_MILLIS % 60).toInt()
        val mm = String.format(Locale.getDefault(), "%02d", m)
        val ss = String.format(Locale.getDefault(), "%02d", s)
        return "$mm:$ss"
    }
}