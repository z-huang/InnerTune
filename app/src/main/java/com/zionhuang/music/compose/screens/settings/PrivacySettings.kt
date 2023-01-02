package com.zionhuang.music.compose.screens.settings

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zionhuang.music.R
import com.zionhuang.music.compose.LocalPlayerAwareWindowInsets
import com.zionhuang.music.compose.component.DefaultDialog
import com.zionhuang.music.compose.component.PreferenceEntry
import com.zionhuang.music.compose.component.SwitchPreference
import com.zionhuang.music.constants.ENABLE_KUGOU
import com.zionhuang.music.constants.PAUSE_SEARCH_HISTORY
import com.zionhuang.music.extensions.mutablePreferenceState
import com.zionhuang.music.repos.SongRepository
import kotlinx.coroutines.launch

@Composable
fun PrivacySettings() {
    val context = LocalContext.current
    val songRepository = SongRepository(context)
    val coroutineScope = rememberCoroutineScope()
    val (pauseSearchHistory, onPauseSearchHistoryChange) = mutablePreferenceState(key = PAUSE_SEARCH_HISTORY, defaultValue = false)
    val (enableKugou, onEnableKugouChange) = mutablePreferenceState(key = ENABLE_KUGOU, defaultValue = true)

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
                        coroutineScope.launch {
                            songRepository.clearSearchHistory()
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