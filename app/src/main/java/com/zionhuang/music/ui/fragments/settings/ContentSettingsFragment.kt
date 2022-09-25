package com.zionhuang.music.ui.fragments.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import com.zionhuang.music.R
import com.zionhuang.music.extensions.preferenceLiveData
import com.zionhuang.music.ui.activities.MainActivity
import com.zionhuang.music.ui.fragments.base.BaseSettingsFragment
import kotlin.system.exitProcess

class ContentSettingsFragment : BaseSettingsFragment() {
    private lateinit var proxyTypePreference: ListPreference
    private lateinit var proxyUrlPreference: EditTextPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_content)

        val proxyEnabledPreference = findPreference<SwitchPreferenceCompat>(getString(R.string.pref_proxy_enabled))!!
        proxyTypePreference = findPreference<ListPreference>(getString(R.string.pref_proxy_type))!!.apply {
            isVisible = proxyEnabledPreference.isChecked
        }
        proxyUrlPreference = findPreference<EditTextPreference>(getString(R.string.pref_proxy_url))!!.apply {
            isVisible = proxyEnabledPreference.isChecked
        }
        findPreference<Preference>(getString(R.string.pref_restart))?.setOnPreferenceClickListener {
            requireContext().startActivity(Intent(requireContext(), MainActivity::class.java))
            exitProcess(0)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireContext().preferenceLiveData(R.string.pref_proxy_enabled, false).observe(viewLifecycleOwner) {
            proxyTypePreference.isVisible = it
            proxyUrlPreference.isVisible = it
        }
    }
}