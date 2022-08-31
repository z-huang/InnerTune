package com.zionhuang.innertube.utils

object TimeParser {
    fun parse(text: String): Int? {
        try {
            val parts = text.split(":").map { it.toInt() }
            if (parts.size == 2) {
                return parts[0] * 60 + parts[1]
            }
            if (parts.size == 3) {
                return parts[0] * 1440 + parts[1] * 60 + parts[2]
            }
        } catch (e: Exception) {
            return null
        }
        return null
    }
}