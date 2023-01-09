package com.zionhuang.music.ui.screens

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.zionhuang.music.R
import com.zionhuang.music.ui.component.AppBarConfig

@Immutable
sealed class Screen(
    @StringRes val titleId: Int,
    @DrawableRes val iconId: Int,
    val route: String,
) {
    object Home : Screen(R.string.title_home, R.drawable.ic_home, "home")
    object Songs : Screen(R.string.title_songs, R.drawable.ic_music_note, "songs")
    object Artists : Screen(R.string.title_artists, R.drawable.ic_artist, "artists")
    object Albums : Screen(R.string.title_albums, R.drawable.ic_album, "albums")
    object Playlists : Screen(R.string.title_playlists, R.drawable.ic_queue_music, "playlists")
}

fun defaultAppBarConfig() = AppBarConfig(
    isRootDestination = true,
    title = {
        Text(
            text = stringResource(R.string.menu_search),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .alpha(0.6f)
                .weight(1f)
        )
    },
    searchable = true
)

fun onlineSearchResultAppBarConfig(query: String) = AppBarConfig(
    title = {
        Text(
            text = query,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    },
    searchable = true
)

fun albumAppBarConfig() = AppBarConfig(
    searchable = false
)

fun artistAppBarConfig() = AppBarConfig(
    searchable = false
)

fun playlistAppBarConfig() = AppBarConfig(
    searchable = false
)

fun settingsAppBarConfig(route: String) = AppBarConfig(
    title = {
        Text(
            text = stringResource(when (route) {
                "settings" -> R.string.title_settings
                "settings/appearance" -> R.string.pref_appearance_title
                "settings/content" -> R.string.pref_content_title
                "settings/player" -> R.string.pref_player_audio_title
                "settings/storage" -> R.string.pref_storage_title
                "settings/general" -> R.string.pref_general_title
                "settings/privacy" -> R.string.pref_privacy_title
                "settings/backup_restore" -> R.string.pref_backup_restore_title
                "settings/about" -> R.string.pref_about_title
                else -> error("Unknown route")
            }),
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    },
    searchable = false
)