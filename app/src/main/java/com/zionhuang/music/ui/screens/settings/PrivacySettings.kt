package com.zionhuang.music.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zionhuang.music.LocalDatabase
import com.zionhuang.music.LocalPlayerAwareWindowInsets
import com.zionhuang.music.R
import com.zionhuang.music.constants.EnableKugouKey
import com.zionhuang.music.constants.PauseSearchHistory
import com.zionhuang.music.ui.component.DefaultDialog
import com.zionhuang.music.ui.component.PreferenceEntry
import com.zionhuang.music.ui.component.SwitchPreference
import com.zionhuang.music.utils.rememberPreference

@Composable
fun PrivacySettings() {
    val database = LocalDatabase.current
    val (pauseSearchHistory, onPauseSearchHistoryChange) = rememberPreference(key = PauseSearchHistory, defaultValue = false)
    val (enableKugou, onEnableKugouChange) = rememberPreference(key = EnableKugouKey, defaultValue = true)

    var showClearHistoryDialog by remember {
        mutableStateOf(false)
    }

    if (showClearHistoryDialog) {
        DefaultDialog(
            onDismiss = { showClearHistoryDialog = false },
            content = {
                Text(
                    text = stringResource(R.string.clear_search_history_question),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 18.dp)
                )
            },
            buttons = {
                TextButton(
                    onClick = { showClearHistoryDialog = false }
                ) {
                    Text(text = stringResource(android.R.string.cancel))
                }

                TextButton(
                    onClick = {
                        showClearHistoryDialog = false
                        database.query {
                            clearSearchHistory()
                        }
                    }
                ) {
                    Text(text = stringResource(android.R.string.ok))
                }
            }
        )
    }

    Column(
        Modifier
            .padding(LocalPlayerAwareWindowInsets.current.asPaddingValues())
            .verticalScroll(rememberScrollState())
    ) {
        SwitchPreference(
            title = stringResource(R.string.pref_pause_search_history_title),
            icon = R.drawable.ic_manage_search,
            checked = pauseSearchHistory,
            onCheckedChange = onPauseSearchHistoryChange
        )
        PreferenceEntry(
            title = stringResource(R.string.pref_clear_search_history_title),
            icon = R.drawable.ic_clear_all,
            onClick = { showClearHistoryDialog = true }
        )
        SwitchPreference(
            title = stringResource(R.string.pref_enable_kugou_title),
            icon = R.drawable.ic_lyrics,
            checked = enableKugou,
            onCheckedChange = onEnableKugouChange
        )
    }
}

enum class AudioQuality {
    AUTO, HIGH, LOW
}