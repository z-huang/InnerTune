package com.zionhuang.music.ui.fragments

import android.content.ComponentName
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.net.toUri
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.transition.MaterialFadeThrough
import com.zionhuang.music.R
import com.zionhuang.music.constants.Constants.APP_URL
import com.zionhuang.music.constants.Constants.NEWPIPE_EXTRACTOR_URL
import com.zionhuang.music.update.UpdateInfo.*
import com.zionhuang.music.update.UpdateService
import com.zionhuang.music.youtube.InfoCache
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.localization.Localization
import java.util.*

class SettingsFragment : PreferenceFragmentCompat() {
    private lateinit var checkForUpdatePreference: Preference
    private lateinit var upgradePreference: Preference

    private var updateService: UpdateService? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            Log.d(TAG, "Service connected")
            if (binder !is UpdateService.UpdateBinder) return
            with(binder.service) {
                updateInfoLiveData.observe(viewLifecycleOwner) { info ->
                    checkForUpdatePreference.isVisible = info !is UpdateAvailable
                    upgradePreference.isVisible = info is UpdateAvailable
                    checkForUpdatePreference.summary = when (info) {
                        is Checking -> getString(R.string.pref_checking_for_updates)
                        is UpToDate -> getString(R.string.pref_up_to_date)
                        is Exception -> getString(R.string.pref_cant_check_for_updates)
                        is NotChecked, is UpdateAvailable -> ""
                    }
                    if (info is UpdateAvailable) {
                        upgradePreference.summary = info.version.toString()
                    }
                }
                checkForUpdate()
                updateService = this
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            updateService = null
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough().apply { duration = 300L }
        exitTransition = MaterialFadeThrough().apply { duration = 300L }
        val systemDefault = getString(R.string.default_localization_key)
        findPreference<ListPreference>(getString(R.string.pref_content_language))?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue !is String) return@setOnPreferenceChangeListener false
            NewPipe.setPreferredLocalization(if (newValue == systemDefault) Localization.fromLocale(Locale.getDefault()) else Localization.fromLocalizationCode(newValue))
            InfoCache.clearCache()
            true
        }
        findPreference<ListPreference>(getString(R.string.pref_content_country))?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue !is String) return@setOnPreferenceChangeListener false
            NewPipe.setPreferredContentCountry(ContentCountry(if (newValue == systemDefault) Locale.getDefault().country else newValue))
            InfoCache.clearCache()
            true
        }
        findPreference<Preference>(getString(R.string.pref_app_version))?.setOnPreferenceClickListener {
            startActivity(Intent(ACTION_VIEW, APP_URL.toUri()))
            true
        }
        findPreference<Preference>(getString(R.string.pref_newpipe_version))?.setOnPreferenceClickListener {
            startActivity(Intent(ACTION_VIEW, NEWPIPE_EXTRACTOR_URL.toUri()))
            true
        }

        checkForUpdatePreference = findPreference(getString(R.string.pref_check_for_updates))!!
        upgradePreference = findPreference(getString(R.string.pref_upgrade))!!

        checkForUpdatePreference.setOnPreferenceClickListener {
            updateService?.checkForUpdate(true)
            true
        }

        val intent = Intent(context, UpdateService::class.java)
        requireContext().startService(intent)
        requireContext().bindService(intent, serviceConnection, BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        requireContext().unbindService(serviceConnection)
        super.onDestroy()
    }

    companion object {
        private const val TAG = "SettingsFragment"
    }
}