package com.zionhuang.music.lyrics

import android.content.Context
import com.zionhuang.kugou.KuGou
import com.zionhuang.music.R
import com.zionhuang.music.extensions.sharedPreferences

object KuGouLyricsProvider : LyricsProvider {
    override val name = "Kugou"
    override fun isEnabled(context: Context): Boolean =
        context.sharedPreferences.getBoolean(context.getString(R.string.pref_enable_kugou), true)

    override suspend fun getLyrics(id: String?, title: String, artist: String, duration: Int): Result<String> =
        KuGou.getLyrics(title, artist, duration)

    override suspend fun getAllLyrics(id: String?, title: String, artist: String, duration: Int): Result<List<String>> =
        KuGou.getAllLyrics(title, artist, duration)
}