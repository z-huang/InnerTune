package com.zionhuang.music.ui.fragments

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.media.audiofx.AudioEffect.*
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import androidx.core.net.toUri
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.*
import com.google.android.material.color.DynamicColors
import com.google.android.material.transition.MaterialFadeThrough
import com.zionhuang.music.R
import com.zionhuang.music.constants.Constants.APP_URL
import com.zionhuang.music.extensions.preferenceLiveData
import com.zionhuang.music.playback.MediaSessionConnection
import com.zionhuang.music.update.UpdateInfo.*
import com.zionhuang.music.utils.InfoCache
import com.zionhuang.music.viewmodels.UpdateViewModel

class SettingsFragment : PreferenceFragmentCompat() {
    private val viewModel by activityViewModels<UpdateViewModel>()

    private val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    private lateinit var checkForUpdatePreference: Preference
    private lateinit var updatePreference: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)

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

        findPreference<ListPreference>(getString(R.string.pref_content_language))?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue !is String) return@setOnPreferenceChangeListener false
            InfoCache.clearCache()
            true
        }
        findPreference<ListPreference>(getString(R.string.pref_content_country))?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue !is String) return@setOnPreferenceChangeListener false
            InfoCache.clearCache()
            true
        }

        val equalizerIntent = Intent(ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
            putExtra(EXTRA_AUDIO_SESSION, MediaSessionConnection.binder?.songPlayer?.player?.audioSessionId)
            putExtra(EXTRA_PACKAGE_NAME, requireContext().packageName)
            putExtra(EXTRA_CONTENT_TYPE, CONTENT_TYPE_MUSIC)
        }
        findPreference<Preference>(getString(R.string.pref_equalizer))?.apply {
            isEnabled = equalizerIntent.resolveActivity(requireContext().packageManager) != null
            setOnPreferenceClickListener {
                activityResultLauncher.launch(equalizerIntent)
                true
            }
        }

        findPreference<Preference>(getString(R.string.pref_app_version))?.setOnPreferenceClickListener {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough().setDuration(resources.getInteger(R.integer.motion_duration_large).toLong())
        exitTransition = MaterialFadeThrough().setDuration(resources.getInteger(R.integer.motion_duration_large).toLong())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val proxyUrlPreference = findPreference<EditTextPreference>(getString(R.string.pref_proxy_url))!!
        requireContext().preferenceLiveData(R.string.pref_proxy_enabled, false).observe(viewLifecycleOwner) {
            proxyUrlPreference.isEnabled = it
        }
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