package com.zionhuang.music.ui.fragments.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.color.DynamicColors
import com.zionhuang.music.R
import com.zionhuang.music.ui.fragments.base.BaseSettingsFragment
import com.zionhuang.music.ui.fragments.dialogs.NavigationTabConfigDialog

class AppearanceSettingsFragment : BaseSettingsFragment() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_appearance)

        val prefFollowSystemAccent = findPreference<SwitchPreferenceCompat>(getString(R.string.pref_follow_system_accent))!!.apply {
            isVisible = DynamicColors.isDynamicColorAvailable()
            setOnPreferenceChangeListener { _, _ ->
                requireActivity().recreate()
                true
            }
        }
        findPreference<ListPreference>(getString(R.string.pref_theme_color))?.apply {
            isVisible = !DynamicColors.isDynamicColorAvailable() || (DynamicColors.isDynamicColorAvailable() && !prefFollowSystemAccent.isChecked)
            setOnPreferenceChangeListener { _, _ ->
                requireActivity().recreate()
                true
            }
        }
        findPreference<ListPreference>(getString(R.string.pref_dark_theme))?.setOnPreferenceChangeListener { _, newValue ->
            setDefaultNightMode((newValue as String).toInt())
            true
        }

        findPreference<Preference>(getString(R.string.pref_nav_tab_config))?.setOnPreferenceClickListener {
            NavigationTabConfigDialog().show(childFragmentManager, null)
            true
        }
    }
}
