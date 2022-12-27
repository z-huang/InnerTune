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
import com.zionhuang.music.constants.Constants.INNERTUBE_COOKIE
import com.zionhuang.music.constants.Constants.VISITOR_DATA
import com.zionhuang.music.extensions.getEnum
import com.zionhuang.music.extensions.sharedPreferences
import com.zionhuang.music.extensions.toInetSocketAddress
import com.zionhuang.music.ui.fragments.settings.StorageSettingsFragment.Companion.VALUE_TO_MB
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.Proxy
import java.util.*

class App : Application(), ImageLoaderFactory, SharedPreferences.OnSharedPreferenceChangeListener {
    @OptIn(DelicateCoroutinesApi::class)
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
        if (languageTag == "zh-TW") {
            KuGou.useTraditionalChinese = true
        }
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

        GlobalScope.launch {
            YouTube.visitorData = sharedPreferences.getString(VISITOR_DATA, null) ?: YouTube.generateVisitorData().getOrNull()?.also {
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
            VISITOR_DATA -> YouTube.visitorData = sharedPreferences.getString(VISITOR_DATA, null) ?: YouTube.DEFAULT_VISITOR_DATA
            INNERTUBE_COOKIE -> YouTube.cookie = sharedPreferences.getString(INNERTUBE_COOKIE, null)
        }
    }

    override fun newImageLoader() = ImageLoader.Builder(this)
        .crossfade(true)
        .respectCacheHeaders(false)
        .allowHardware(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
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