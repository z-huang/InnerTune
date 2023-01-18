package com.zionhuang.music

import android.app.Application
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.core.content.edit
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.YouTubeLocale
import com.zionhuang.kugou.KuGou
import com.zionhuang.music.constants.*
import com.zionhuang.music.extensions.getEnum
import com.zionhuang.music.extensions.sharedPreferences
import com.zionhuang.music.extensions.toInetSocketAddress
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.Proxy
import java.util.*

@HiltAndroidApp
class App : Application(), ImageLoaderFactory, SharedPreferences.OnSharedPreferenceChangeListener {
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        INSTANCE = this

        val locale = Locale.getDefault()
        val languageTag = locale.toLanguageTag().replace("-Hant", "") // replace zh-Hant-* to zh-*
        YouTube.locale = YouTubeLocale(
            gl = sharedPreferences.getString(CONTENT_COUNTRY, SYSTEM_DEFAULT)
                .takeIf { it != SYSTEM_DEFAULT }
                ?: locale.country.takeIf { it in CountryCodeToName }
                ?: "US",
            hl = sharedPreferences.getString(CONTENT_LANGUAGE, SYSTEM_DEFAULT)
                .takeIf { it != SYSTEM_DEFAULT }
                ?: locale.language.takeIf { it in LanguageCodeToName }
                ?: languageTag.takeIf { it in LanguageCodeToName }
                ?: "en"
        )
        if (languageTag == "zh-TW") {
            KuGou.useTraditionalChinese = true
        }
        Log.d("App", "${YouTube.locale}")

        if (sharedPreferences.getBoolean(PROXY_ENABLED, false)) {
            try {
                YouTube.proxy = Proxy(
                    sharedPreferences.getEnum(PROXY_TYPE, Proxy.Type.HTTP),
                    sharedPreferences.getString(PROXY_URL, "")!!.toInetSocketAddress()
                )
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to parse proxy url.", LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }

        GlobalScope.launch {
            YouTube.visitorData = sharedPreferences.getString(VISITOR_DATA, null)
                ?: YouTube.generateVisitorData().getOrNull()?.also {
                    sharedPreferences.edit {
                        putString(VISITOR_DATA, it)
                    }
                } ?: YouTube.DEFAULT_VISITOR_DATA
        }
        YouTube.cookie = sharedPreferences.getString(INNERTUBE_COOKIE, null)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            VISITOR_DATA -> {
                YouTube.visitorData = sharedPreferences.getString(VISITOR_DATA, null)
                    ?: YouTube.DEFAULT_VISITOR_DATA
            }
            INNERTUBE_COOKIE -> {
                YouTube.cookie = sharedPreferences.getString(INNERTUBE_COOKIE, null)
            }
        }
    }

    override fun newImageLoader() = ImageLoader.Builder(this)
        .crossfade(true)
        .respectCacheHeaders(false)
        .allowHardware(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
        .diskCache(
            DiskCache.Builder()
                .directory(cacheDir.resolve("coil"))
                .maxSizeBytes(sharedPreferences.getInt(MAX_IMAGE_CACHE_SIZE, 512) * 1024 * 1024L)
                .build()
        )
        .build()

    companion object {
        lateinit var INSTANCE: App
    }
}