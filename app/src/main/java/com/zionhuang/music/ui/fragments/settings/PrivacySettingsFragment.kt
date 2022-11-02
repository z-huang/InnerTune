package com.zionhuang.music.ui.fragments.settings

import android.os.Bundle
import androidx.preference.Preference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zionhuang.music.R
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.ui.fragments.base.BaseSettingsFragment
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PrivacySettingsFragment : BaseSettingsFragment() {
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_privacy)

        findPreference<Preference>(getString(R.string.pref_clear_search_history))?.setOnPreferenceClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setMessage(R.string.clear_search_history_question)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    GlobalScope.launch {
                        SongRepository(requireContext()).clearSearchHistory()
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
            true
        }
    }
}