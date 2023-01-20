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
import com.zionhuang.music.constants.AudioNormalizationKey
import com.zionhuang.music.constants.AudioQualityKey
import com.zionhuang.music.constants.PersistentQueueKey
import com.zionhuang.music.constants.SkipSilenceKey
import com.zionhuang.music.ui.component.EnumListPreference
import com.zionhuang.music.ui.component.SwitchPreference
import com.zionhuang.music.utils.rememberEnumPreference
import com.zionhuang.music.utils.rememberPreference

@Composable
fun PlayerSettings() {
    val (audioQuality, onAudioQualityChange) = rememberEnumPreference(key = AudioQualityKey, defaultValue = AudioQuality.AUTO)
    val (persistentQueue, onPersistentQueueChange) = rememberPreference(key = PersistentQueueKey, defaultValue = true)
    val (skipSilence, onSkipSilenceChange) = rememberPreference(key = SkipSilenceKey, defaultValue = true)
    val (audioNormalization, onAudioNormalizationChange) = rememberPreference(key = AudioNormalizationKey, defaultValue = true)

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
