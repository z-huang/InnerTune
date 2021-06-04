package com.zionhuang.music.ui.fragments

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.os.Bundle
import androidx.core.net.toUri
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.transition.MaterialFadeThrough
import com.zionhuang.music.R
import com.zionhuang.music.constants.Constants.APP_URL
import com.zionhuang.music.constants.Constants.NEWPIPE_EXTRACTOR_URL
import com.zionhuang.music.youtube.newpipe.InfoCache
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.localization.Localization
import java.util.*

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough().apply { duration = 300L }
        exitTransition = MaterialFadeThrough().apply { duration = 300L }
        val systemDefault = getString(R.string.default_localization_key)
        findPreference<ListPreference>(getString(R.string.pref_content_language))?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue !is String) return@setOnPreferenceChangeListener false
            NewPipe.setPreferredLocalization(
                if (newValue == systemDefault)
                    Localization.fromLocale(Locale.getDefault())
                else
                    Localization.fromLocalizationCode(newValue)
            )
            InfoCache.clearCache()
            true
        }
        findPreference<ListPreference>(getString(R.string.pref_content_country))?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue !is String) return@setOnPreferenceChangeListener false
            NewPipe.setPreferredContentCountry(
                ContentCountry(
                    if (newValue == systemDefault)
                        Locale.getDefault().country
                    else newValue
                )
            )
            InfoCache.clearCache()
            true
        }
        findPreference<Preference>(getString(R.string.pref_app_version))?.setOnPreferenceClickListener {
            startActivity(Intent(ACTION_VIEW, APP_URL.toUri()))
            true
        }
        findPreference<Preference>(getString(R.string.pref_newpipe_version))?.setOnPreferenceClickListener {
            startActivity(Intent(ACTION_VIEW, NEWPIPE_EXTRACTOR_URL.toUri()))
            true
        }
    }

    companion object {
        private const val TAG = "SettingsFragment"
    }
}