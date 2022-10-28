package com.zionhuang.music.ui.fragments

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceScreen
import com.zionhuang.music.R
import com.zionhuang.music.ui.fragments.base.BaseSettingsFragment

class SettingsFragment : BaseSettingsFragment() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_main)

        findPreference<PreferenceScreen>(getString(R.string.pref_appearance))?.setOnPreferenceClickListener {
            findNavController().navigate(R.id.appearanceSettingsFragment)
            true
        }
        findPreference<PreferenceScreen>(getString(R.string.pref_content))?.setOnPreferenceClickListener {
            findNavController().navigate(R.id.contentSettingsFragment)
            true
        }
        findPreference<PreferenceScreen>(getString(R.string.pref_player_audio))?.setOnPreferenceClickListener {
            findNavController().navigate(R.id.playerAudioSettingsFragment)
            true
        }
        findPreference<PreferenceScreen>(getString(R.string.pref_storage))?.setOnPreferenceClickListener {
            findNavController().navigate(R.id.storageSettingsFragment)
            true
        }
        findPreference<PreferenceScreen>(getString(R.string.pref_general))?.setOnPreferenceClickListener {
            findNavController().navigate(R.id.generalSettingsFragment)
            true
        }
        findPreference<PreferenceScreen>(getString(R.string.pref_privacy))?.setOnPreferenceClickListener {
            findNavController().navigate(R.id.privacySettingsFragment)
            true
        }
        findPreference<PreferenceScreen>(getString(R.string.pref_backup_restore))?.setOnPreferenceClickListener {
            findNavController().navigate(R.id.backupRestoreSettingsFragment)
            true
        }
        findPreference<PreferenceScreen>(getString(R.string.pref_about))?.setOnPreferenceClickListener {
            findNavController().navigate(R.id.aboutFragment)
            true
        }
    }
}