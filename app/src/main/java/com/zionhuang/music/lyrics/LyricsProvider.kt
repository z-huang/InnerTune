package com.zionhuang.music.lyrics

import android.content.Context

interface LyricsProvider {
    val name: String
    fun isEnabled(context: Context): Boolean
    suspend fun getLyrics(id: String?, title: String, artist: String, duration: Int): Result<String>
    suspend fun getAllLyrics(id: String?, title: String, artist: String, duration: Int): Result<List<String>>
}