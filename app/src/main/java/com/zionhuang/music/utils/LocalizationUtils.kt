package com.zionhuang.music.utils

import android.content.Context
import com.zionhuang.music.R
import com.zionhuang.music.extensions.sharedPreferences
import java.util.*


object LocalizationUtils {
    fun getAppLocale(context: Context): Locale {
        val lang = context.sharedPreferences.getString(context.getString(R.string.pref_app_language), "en")
        return if (lang == context.getString(R.string.default_localization_key)) {
            Locale.getDefault()
        } else if (lang!!.matches(".*-.*".toRegex())) {
            lang.split("-").toTypedArray().let {
                Locale(it[0], it[1])
            }
        } else {
            Locale(lang)
        }
    }
}