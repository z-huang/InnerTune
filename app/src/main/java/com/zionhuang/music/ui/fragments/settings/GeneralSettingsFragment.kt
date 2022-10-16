package com.zionhuang.music.ui.fragments.settings

import android.os.Bundle
import com.zionhuang.music.R
import com.zionhuang.music.ui.fragments.base.BaseSettingsFragment

class GeneralSettingsFragment : BaseSettingsFragment() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_general)
    }
}