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
import com.zionhuang.music.constants.AUTO_ADD_TO_LIBRARY
import com.zionhuang.music.constants.AUTO_DOWNLOAD
import com.zionhuang.music.constants.EXPAND_ON_PLAY
import com.zionhuang.music.constants.NOTIFICATION_MORE_ACTION
import com.zionhuang.music.extensions.mutablePreferenceState
import com.zionhuang.music.ui.component.SwitchPreference

@Composable
fun GeneralSettings() {
    val (autoAddToLibrary, onAutoAddToLibraryChange) = mutablePreferenceState(key = AUTO_ADD_TO_LIBRARY, defaultValue = true)
    val (autoDownload, onAutoDownloadChange) = mutablePreferenceState(key = AUTO_DOWNLOAD, defaultValue = false)
    val (expandOnPlay, onExpandOnPlayChange) = mutablePreferenceState(key = EXPAND_ON_PLAY, defaultValue = false)
    val (notificationMoreAction, onNotificationMoreActionChange) = mutablePreferenceState(key = NOTIFICATION_MORE_ACTION, defaultValue = true)

    Column(
        Modifier
            .padding(LocalPlayerAwareWindowInsets.current.asPaddingValues())
            .verticalScroll(rememberScrollState())
    ) {
        SwitchPreference(
            title = stringResource(R.string.pref_auto_add_song_title),
            description = stringResource(R.string.pref_auto_add_song_summary),
            icon = R.drawable.ic_library_add,
            checked = autoAddToLibrary,
            onCheckedChange = onAutoAddToLibraryChange
        )
        SwitchPreference(
            title = stringResource(R.string.pref_auto_download_title),
            description = stringResource(R.string.pref_auto_download_summary),
            icon = R.drawable.ic_save_alt,
            checked = autoDownload,
            onCheckedChange = onAutoDownloadChange
        )
        SwitchPreference(
            title = stringResource(R.string.pref_expand_on_play_title),
            icon = R.drawable.ic_open_in_full,
            checked = expandOnPlay,
            onCheckedChange = onExpandOnPlayChange
        )
        SwitchPreference(
            title = stringResource(R.string.pref_notification_more_action_title),
            description = stringResource(R.string.pref_notification_more_action_summary),
            icon = R.drawable.ic_notifications,
            checked = notificationMoreAction,
            onCheckedChange = onNotificationMoreActionChange
        )
    }
}
