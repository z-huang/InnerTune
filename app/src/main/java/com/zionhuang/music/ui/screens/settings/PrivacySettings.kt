package com.zionhuang.music.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.zionhuang.music.LocalDatabase
import com.zionhuang.music.LocalPlayerAwareWindowInsets
import com.zionhuang.music.R
import com.zionhuang.music.constants.EnableKugouKey
import com.zionhuang.music.constants.PauseListenHistoryKey
import com.zionhuang.music.constants.PauseSearchHistoryKey
import com.zionhuang.music.ui.component.DefaultDialog
import com.zionhuang.music.ui.component.PreferenceEntry
import com.zionhuang.music.ui.component.SwitchPreference
import com.zionhuang.music.utils.rememberPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val database = LocalDatabase.current
    val (pauseListenHistory, onPauseListenHistoryChange) = rememberPreference(key = PauseListenHistoryKey, defaultValue = false)
    val (pauseSearchHistory, onPauseSearchHistoryChange) = rememberPreference(key = PauseSearchHistoryKey, defaultValue = false)
    val (enableKugou, onEnableKugouChange) = rememberPreference(key = EnableKugouKey, defaultValue = true)

    var showClearHistoryDialog by remember {
        mutableStateOf(false)
    }

    if (showClearHistoryDialog) {
        DefaultDialog(
            onDismiss = { showClearHistoryDialog = false },
            content = {
                Text(
                    text = stringResource(R.string.clear_search_history_confirm),
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
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
            .verticalScroll(rememberScrollState())
    ) {
        SwitchPreference(
            title = stringResource(R.string.pause_listen_history),
            icon = R.drawable.history,
            checked = pauseListenHistory,
            onCheckedChange = onPauseListenHistoryChange
        )
        SwitchPreference(
            title = stringResource(R.string.pause_search_history),
            icon = R.drawable.manage_search,
            checked = pauseSearchHistory,
            onCheckedChange = onPauseSearchHistoryChange
        )
        PreferenceEntry(
            title = stringResource(R.string.clear_search_history),
            icon = R.drawable.clear_all,
            onClick = { showClearHistoryDialog = true }
        )
        SwitchPreference(
            title = stringResource(R.string.enable_kugou),
            icon = R.drawable.lyrics,
            checked = enableKugou,
            onCheckedChange = onEnableKugouChange
        )
    }

    TopAppBar(
        title = { Text(stringResource(R.string.privacy)) },
        navigationIcon = {
            IconButton(onClick = navController::navigateUp) {
                Icon(
                    painterResource(R.drawable.arrow_back),
                    contentDescription = null
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}
