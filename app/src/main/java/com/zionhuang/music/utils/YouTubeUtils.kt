package com.zionhuang.music.utils

private const val MS_FORMAT = "%1\$d:%2$02d"
private const val HMS_FORMAT = "%1\$d:%2$02d:%3$02d"

/**
 * Convert duration in seconds to formatted time string
 *
 * @param duration in milliseconds
 * @return formatted string
 */

fun makeTimeString(duration: Long?): String {
    if (duration == null) return "0:00"
    var sec = duration / 1000
    val hour = sec / 3600
    sec %= 3600
    val minute = (sec / 60).toInt()
    sec %= 60
    return if (hour == 0L) MS_FORMAT.format(minute, sec) else HMS_FORMAT.format(hour, minute, sec)
}
