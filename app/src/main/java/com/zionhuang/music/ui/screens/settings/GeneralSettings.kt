package com.zionhuang.music.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.zionhuang.music.LocalPlayerAwareWindowInsets
import com.zionhuang.music.R
import com.zionhuang.music.constants.AutoAddToLibraryKey
import com.zionhuang.music.constants.AutoDownloadKey
import com.zionhuang.music.constants.ExpandOnPlayKey
import com.zionhuang.music.constants.NotificationMoreActionKey
import com.zionhuang.music.ui.component.SwitchPreference
import com.zionhuang.music.utils.rememberPreference

@Composable
fun GeneralSettings() {
    val (autoAddToLibrary, onAutoAddToLibraryChange) = rememberPreference(key = AutoAddToLibraryKey, defaultValue = true)
    val (autoDownload, onAutoDownloadChange) = rememberPreference(key = AutoDownloadKey, defaultValue = false)
    val (expandOnPlay, onExpandOnPlayChange) = rememberPreference(key = ExpandOnPlayKey, defaultValue = false)
    val (notificationMoreAction, onNotificationMoreActionChange) = rememberPreference(key = NotificationMoreActionKey, defaultValue = true)

    Column(
        Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
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
