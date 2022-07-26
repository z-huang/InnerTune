package com.zionhuang.music

import android.app.Application
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.YouTubeLocale
import com.zionhuang.music.extensions.sharedPreferences
import com.zionhuang.music.playback.MediaSessionConnection
import com.zionhuang.music.utils.getPreferredContentCountry
import com.zionhuang.music.utils.getPreferredLocalization
import com.zionhuang.music.youtube.NewPipeDownloader
import org.schabi.newpipe.extractor.NewPipe
import java.util.*

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        NewPipe.init(
            NewPipeDownloader.init(),
            getPreferredLocalization(this),
            getPreferredContentCountry(this)
        )

        val systemDefault = getString(R.string.default_localization_key)
        YouTube.locale = YouTubeLocale(
            gl = sharedPreferences.getString(getString(R.string.pref_content_country), systemDefault).takeIf { it != systemDefault } ?: Locale.getDefault().country,
            hl = sharedPreferences.getString(getString(R.string.pref_content_language), systemDefault).takeIf { it != systemDefault } ?: Locale.getDefault().toLanguageTag()
        )
        if (sharedPreferences.getBoolean(getString(R.string.pref_proxy_enabled), false)) {
            try {
                YouTube.setProxyUrl(sharedPreferences.getString(getString(R.string.pref_proxy_url), null) ?: "")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        MediaSessionConnection.connect(this)
    }

    companion object {
        lateinit var INSTANCE: App
    }
}