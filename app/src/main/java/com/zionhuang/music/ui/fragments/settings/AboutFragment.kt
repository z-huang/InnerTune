package com.zionhuang.music.ui.fragments.settings

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.os.Bundle
import androidx.core.net.toUri
import androidx.preference.Preference
import com.zionhuang.music.R
import com.zionhuang.music.constants.Constants.GITHUB_URL
import com.zionhuang.music.ui.fragments.base.BaseSettingsFragment

class AboutFragment : BaseSettingsFragment() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_about)

        findPreference<Preference>(getString(R.string.pref_github))?.setOnPreferenceClickListener {
            startActivity(Intent(ACTION_VIEW, GITHUB_URL.toUri()))
            true
        }
    }
}