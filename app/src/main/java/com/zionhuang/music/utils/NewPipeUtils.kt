package com.zionhuang.music.utils

import android.content.Context
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.zionhuang.music.R
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.localization.Localization
import java.util.*

fun getPreferredLocalization(context: Context): Localization {
    val systemDefault = context.getString(R.string.default_localization_key)
    return getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_content_language), systemDefault)!!.let {
        if (it == systemDefault) Localization.fromLocale(Locale.getDefault())
        else Localization.fromLocalizationCode(it)
    }
}

fun getPreferredContentCountry(context: Context): ContentCountry {
    val systemDefault = context.getString(R.string.default_localization_key)
    return getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_content_country), systemDefault)!!.let {
        if (it == systemDefault) ContentCountry(Locale.getDefault().country)
        else ContentCountry(it)
    }
}