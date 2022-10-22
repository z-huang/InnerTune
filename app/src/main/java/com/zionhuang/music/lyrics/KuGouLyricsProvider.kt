package com.zionhuang.music.lyrics

import com.zionhuang.kugou.KuGou

object KuGouLyricsProvider : LyricsProvider {
    override suspend fun getLyrics(id: String, title: String, artist: String, duration: Int): Result<String> =
        KuGou.getLyrics(title, artist, duration)
}