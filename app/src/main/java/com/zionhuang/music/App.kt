package com.zionhuang.music

import android.app.Application
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.core.content.edit
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.YouTubeClient
import com.zionhuang.innertube.models.YouTubeLocale
import com.zionhuang.music.extensions.getEnum
import com.zionhuang.music.extensions.sharedPreferences
import com.zionhuang.music.extensions.toInetSocketAddress
import com.zionhuang.music.ui.fragments.settings.CacheSettingsFragment.Companion.VALUE_TO_MB
import java.net.Proxy
import java.util.*

class App : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        INSTANCE = this

        val systemDefault = getString(R.string.default_localization_key)
        val locale = Locale.getDefault()
        val languageTag = locale.toLanguageTag().replace("-Hant", "") // replace zh-Hant-* to zh-*
        val languageCodes = resources.getStringArray(R.array.language_codes)
        val countryCodes = resources.getStringArray(R.array.country_codes)
        YouTube.locale = YouTubeLocale(
            gl = sharedPreferences.getString(getString(R.string.pref_content_country), systemDefault).takeIf { it != systemDefault }
                ?: locale.country.takeIf { it in countryCodes }
                ?: "US",
            hl = sharedPreferences.getString(getString(R.string.pref_content_language), systemDefault).takeIf { it != systemDefault }
                ?: locale.language.takeIf { it in languageCodes }
                ?: languageTag.takeIf { it in languageCodes }
                ?: "en"
        )
        Log.d("App", "${YouTube.locale}")

        if (sharedPreferences.getBoolean(getString(R.string.pref_proxy_enabled), false)) {
            try {
                val socketAddress = sharedPreferences.getString(getString(R.string.pref_proxy_url), "")!!.toInetSocketAddress()
                YouTube.proxy = Proxy(
                    sharedPreferences.getEnum(getString(R.string.pref_proxy_type), Proxy.Type.HTTP),
                    socketAddress
                )
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to parse proxy url.", LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }

        YouTube.visitorData = sharedPreferences.getString(getString(R.string.pref_visitor_data), null) ?: YouTubeClient.generateVisitorData().also {
            sharedPreferences.edit {
                putString(getString(R.string.pref_visitor_data), it)
            }
        }
    }

    override fun newImageLoader() = ImageLoader.Builder(this)
        .crossfade(true)
        .diskCache(
            DiskCache.Builder()
                .directory(cacheDir.resolve("coil"))
                .maxSizeBytes(
                    size = (VALUE_TO_MB.getOrNull(
                        sharedPreferences.getInt(getString(R.string.pref_image_max_cache_size), 0)
                    ) ?: 1024) * 1024 * 1024L)
                .build()
        )
        .build()

    companion object {
        lateinit var INSTANCE: App
    }
}