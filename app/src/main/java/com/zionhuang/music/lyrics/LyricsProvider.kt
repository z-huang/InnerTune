package com.zionhuang.music.lyrics

interface LyricsProvider {
    suspend fun getLyrics(id: String, title: String, artist: String, duration: Int): Result<String>
}