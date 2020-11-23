package com.zionhuang.music.ui.fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.zionhuang.music.R

class SettingsFragment : PreferenceFragmentCompat() {
    companion object {
        private const val TAG = "SettingsFragment"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
    }
}