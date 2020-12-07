package com.zionhuang.music.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.preference.PreferenceFragmentCompat
import com.zionhuang.music.R
import com.zionhuang.music.ui.activities.MainActivity

class SettingsFragment : PreferenceFragmentCompat() {
    companion object {
        private const val TAG = "SettingsFragment"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (requireActivity() as MainActivity).tabLayout.isVisible = false
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
    }
}