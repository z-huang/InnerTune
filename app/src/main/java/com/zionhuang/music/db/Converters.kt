package com.zionhuang.music.db

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneOffset.UTC)

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime): Long =
        date.atZone(ZoneOffset.UTC).toInstant().toEpochMilli()
}