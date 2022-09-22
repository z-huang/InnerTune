package com.zionhuang.music.ui.fragments.settings

import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.SwitchPreferenceCompat
import com.zionhuang.music.R
import com.zionhuang.music.extensions.preferenceLiveData
import com.zionhuang.music.ui.fragments.base.BaseSettingsFragment

class ContentSettingsFragment : BaseSettingsFragment() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_content)

        findPreference<ListPreference>(getString(R.string.pref_content_language))?.setOnPreferenceChangeListener { preference, newValue ->
            if ((preference as ListPreference).value == newValue) return@setOnPreferenceChangeListener false
            Toast.makeText(requireContext(), R.string.toast_restart_to_take_effect, LENGTH_SHORT).show()
            true
        }
        findPreference<ListPreference>(getString(R.string.pref_content_country))?.setOnPreferenceChangeListener { preference, newValue ->
            if ((preference as ListPreference).value == newValue) return@setOnPreferenceChangeListener false
            Toast.makeText(requireContext(), R.string.toast_restart_to_take_effect, LENGTH_SHORT).show()
            true
        }

        findPreference<SwitchPreferenceCompat>(getString(R.string.pref_proxy_enabled))?.setOnPreferenceChangeListener { preference, newValue ->
            if ((preference as SwitchPreferenceCompat).isChecked == newValue) return@setOnPreferenceChangeListener false
            Toast.makeText(requireContext(), R.string.toast_restart_to_take_effect, LENGTH_SHORT).show()
            true
        }
        findPreference<EditTextPreference>(getString(R.string.pref_proxy_url))?.setOnPreferenceChangeListener { preference, newValue ->
            if ((preference as EditTextPreference).text == newValue) return@setOnPreferenceChangeListener false
            Toast.makeText(requireContext(), R.string.toast_restart_to_take_effect, LENGTH_SHORT).show()
            true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val proxyUrlPreference = findPreference<EditTextPreference>(getString(R.string.pref_proxy_url))!!
        requireContext().preferenceLiveData(R.string.pref_proxy_enabled, false).observe(viewLifecycleOwner) {
            proxyUrlPreference.isEnabled = it
        }
    }
}