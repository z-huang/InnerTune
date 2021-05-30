package com.zionhuang.music.ui.fragments

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.os.Bundle
import androidx.core.net.toUri
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.transition.MaterialFadeThrough
import com.zionhuang.music.R
import com.zionhuang.music.constants.Constants.APP_URL
import com.zionhuang.music.constants.Constants.NEWPIPE_EXTRACTOR_URL

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough().apply { duration = 300L }
        exitTransition = MaterialFadeThrough().apply { duration = 300L }
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