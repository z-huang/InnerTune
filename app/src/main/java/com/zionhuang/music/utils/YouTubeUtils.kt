package com.zionhuang.music.utils

import com.google.api.services.youtube.model.ThumbnailDetails

private const val MS_FORMAT = """%2${"$"}d:%3$02d"""
private const val HMS_FORMAT = """%1${"$"}d:%2$02d:%3$02d"""

/**
 * Convert duration in seconds to formatted time string
 *
 * @param duration in seconds
 * @return formatted string
 */
@Suppress("NAME_SHADOWING")
fun makeTimeString(duration: Long): String {
    var duration = duration
    val hours = duration / 3600
    duration %= 3600
    val minutes = (duration / 60).toInt()
    duration %= 60
    val secs = duration.toInt()
    return String.format(if (hours == 0L) MS_FORMAT else HMS_FORMAT, hours, minutes, secs)
}

fun getMaxResThumbnailUrl(thumbnails: ThumbnailDetails): String? =
        thumbnails.maxres?.url
                ?: thumbnails.high?.url
                ?: thumbnails.medium?.url
                ?: thumbnails.standard?.url
                ?: thumbnails.default?.url
