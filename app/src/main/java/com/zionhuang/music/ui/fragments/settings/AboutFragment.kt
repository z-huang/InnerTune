package com.zionhuang.music.ui.fragments.settings

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.zionhuang.music.R
import com.zionhuang.music.constants.Constants.APP_URL
import com.zionhuang.music.ui.fragments.UpdateFragmentDirections
import com.zionhuang.music.update.UpdateInfo.*
import com.zionhuang.music.viewmodels.UpdateViewModel

class AboutFragment : PreferenceFragmentCompat() {
    private val viewModel by activityViewModels<UpdateViewModel>()

    private lateinit var checkForUpdatePreference: Preference
    private lateinit var updatePreference: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_about)

        findPreference<Preference>(getString(R.string.pref_github))?.setOnPreferenceClickListener {
            startActivity(Intent(ACTION_VIEW, APP_URL.toUri()))
            true
        }

        checkForUpdatePreference = findPreference(getString(R.string.pref_check_for_updates))!!
        updatePreference = findPreference(getString(R.string.pref_update))!!

        checkForUpdatePreference.setOnPreferenceClickListener {
            viewModel.checkForUpdate(true)
            true
        }

        updatePreference.setOnPreferenceClickListener {
            findNavController().navigate(UpdateFragmentDirections.openUpdateFragment())
            true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.updateInfo.observe(viewLifecycleOwner) { info ->
            checkForUpdatePreference.isVisible = info !is UpdateAvailable
            updatePreference.isVisible = info is UpdateAvailable
            checkForUpdatePreference.summary = when (info) {
                is Checking -> getString(R.string.pref_checking_for_updates)
                is UpToDate -> getString(R.string.pref_up_to_date)
                is Exception -> getString(R.string.pref_cant_check_for_updates)
                is NotChecked, is UpdateAvailable -> ""
            }
            if (info is UpdateAvailable) {
                updatePreference.summary = info.version.toString()
            }
        }

        viewModel.checkForUpdate()
    }
}