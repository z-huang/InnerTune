package com.zionhuang.music.ui.fragments.settings

import android.content.Intent
import android.media.audiofx.AudioEffect.*
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.Preference
import com.zionhuang.music.R
import com.zionhuang.music.playback.MediaSessionConnection
import com.zionhuang.music.ui.fragments.base.BaseSettingsFragment

class PlayerAudioSettingsFragment : BaseSettingsFragment() {
    private val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_player_audio)

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
    }
}