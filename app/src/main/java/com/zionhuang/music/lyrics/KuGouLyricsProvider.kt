package com.zionhuang.music.lyrics

import android.content.Context
import com.zionhuang.kugou.KuGou
import com.zionhuang.music.constants.ENABLE_KUGOU
import com.zionhuang.music.extensions.sharedPreferences

object KuGouLyricsProvider : LyricsProvider {
    override val name = "Kugou"
    override fun isEnabled(context: Context): Boolean =
        context.sharedPreferences.getBoolean(ENABLE_KUGOU, true)

    override suspend fun getLyrics(id: String?, title: String, artist: String, duration: Int): Result<String> =
        KuGou.getLyrics(title, artist, duration)

    override suspend fun getAllLyrics(id: String?, title: String, artist: String, duration: Int, callback: (String) -> Unit) {
        KuGou.getAllLyrics(title, artist, duration, callback)
    }
}