package com.zionhuang.music.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.zionhuang.music.LocalPlayerAwareWindowInsets
import com.zionhuang.music.R
import com.zionhuang.music.constants.AUDIO_NORMALIZATION
import com.zionhuang.music.constants.AUDIO_QUALITY
import com.zionhuang.music.constants.PERSISTENT_QUEUE
import com.zionhuang.music.constants.SKIP_SILENCE
import com.zionhuang.music.extensions.mutablePreferenceState
import com.zionhuang.music.ui.component.EnumListPreference
import com.zionhuang.music.ui.component.SwitchPreference

@Composable
fun PlayerSettings() {
    val (audioQuality, onAudioQualityChange) = mutablePreferenceState(key = AUDIO_QUALITY, defaultValue = AudioQuality.AUTO)
    val (persistentQueue, onPersistentQueueChange) = mutablePreferenceState(key = PERSISTENT_QUEUE, defaultValue = true)
    val (skipSilence, onSkipSilenceChange) = mutablePreferenceState(key = SKIP_SILENCE, defaultValue = true)
    val (audioNormalization, onAudioNormalizationChange) = mutablePreferenceState(key = AUDIO_NORMALIZATION, defaultValue = true)

    Column(
        Modifier
            .padding(LocalPlayerAwareWindowInsets.current.asPaddingValues())
            .verticalScroll(rememberScrollState())
    ) {
        EnumListPreference(
            title = stringResource(R.string.pref_audio_quality_title),
            icon = R.drawable.ic_graphic_eq,
            selectedValue = audioQuality,
            onValueSelected = onAudioQualityChange,
            valueText = {
                when (it) {
                    AudioQuality.AUTO -> stringResource(R.string.audio_quality_auto)
                    AudioQuality.HIGH -> stringResource(R.string.audio_quality_high)
                    AudioQuality.LOW -> stringResource(R.string.audio_quality_low)
                }
            }
        )
        SwitchPreference(
            title = stringResource(R.string.pref_persistent_queue_title),
            icon = R.drawable.ic_queue_music,
            checked = persistentQueue,
            onCheckedChange = onPersistentQueueChange
        )
        SwitchPreference(
            title = stringResource(R.string.pref_skip_silence_title),
            icon = R.drawable.ic_skip_next,
            checked = skipSilence,
            onCheckedChange = onSkipSilenceChange
        )
        SwitchPreference(
            title = stringResource(R.string.pref_audio_normalization_title),
            icon = R.drawable.ic_volume_up,
            checked = audioNormalization,
            onCheckedChange = onAudioNormalizationChange
        )
    }
}
