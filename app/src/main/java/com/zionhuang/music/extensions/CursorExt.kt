package com.zionhuang.music.extensions

import android.database.Cursor

fun Cursor.forEach(action: Cursor.() -> Unit) = use {
    if (moveToFirst()) {
        do {
            action(this)
        } while (moveToNext())
    }
}

inline operator fun <reified T> Cursor.get(name: String): T {
    val index = getColumnIndexOrThrow(name)
    return when (T::class) {
        Short::class -> getShort(index) as T
        Int::class -> getInt(index) as T
        Long::class -> getLong(index) as T
        Boolean::class -> (getInt(index) == 1) as T
        String::class -> getString(index) as T
        Float::class -> getFloat(index) as T
        Double::class -> getDouble(index) as T
        ByteArray::class -> getBlob(index) as T
        else -> throw IllegalStateException("Unknown class ${T::class.java.simpleName}")
    }
}