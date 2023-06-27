package com.zionhuang.music.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.zionhuang.music.LocalPlayerAwareWindowInsets
import com.zionhuang.music.R
import com.zionhuang.music.ui.component.PreferenceEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    Column(
        modifier = Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
            .verticalScroll(rememberScrollState())
    ) {
        PreferenceEntry(
            title = stringResource(R.string.appearance),
            icon = R.drawable.palette,
            onClick = { navController.navigate("settings/appearance") }
        )
        PreferenceEntry(
            title = stringResource(R.string.content),
            icon = R.drawable.language,
            onClick = { navController.navigate("settings/content") }
        )
        PreferenceEntry(
            title = stringResource(R.string.player_and_audio),
            icon = R.drawable.play,
            onClick = { navController.navigate("settings/player") }
        )
        PreferenceEntry(
            title = stringResource(R.string.storage),
            icon = R.drawable.storage,
            onClick = { navController.navigate("settings/storage") }
        )
        PreferenceEntry(
            title = stringResource(R.string.privacy),
            icon = R.drawable.security,
            onClick = { navController.navigate("settings/privacy") }
        )
        PreferenceEntry(
            title = stringResource(R.string.backup_restore),
            icon = R.drawable.restore,
            onClick = { navController.navigate("settings/backup_restore") }
        )
        PreferenceEntry(
            title = stringResource(R.string.about),
            icon = R.drawable.info,
            onClick = { navController.navigate("settings/about") }
        )
    }

    TopAppBar(
        title = { Text(stringResource(R.string.settings)) },
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
