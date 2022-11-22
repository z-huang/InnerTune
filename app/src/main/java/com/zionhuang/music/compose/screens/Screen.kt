package com.zionhuang.music.compose.screens

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.zionhuang.music.R
import com.zionhuang.music.compose.component.AppBarState

sealed class Screen(
    @StringRes val titleId: Int,
    @DrawableRes val iconId: Int,
    val route: String,
    val appBarState: AppBarState,
) {
    object Home : Screen(R.string.title_home, R.drawable.ic_home, "home",
        AppBarState(
            navigationIcon = R.drawable.ic_search,
            canSearch = true
        )
    )

    object Songs : Screen(R.string.title_songs, R.drawable.ic_music_note, "songs",
        AppBarState(
            navigationIcon = R.drawable.ic_search,
            canSearch = true
        )
    )

    object Artists : Screen(R.string.title_artists, R.drawable.ic_artist, "artists",
        AppBarState(
            navigationIcon = R.drawable.ic_search,
            canSearch = true
        )
    )

    object Albums : Screen(R.string.title_albums, R.drawable.ic_album, "albums",
        AppBarState(
            navigationIcon = R.drawable.ic_search,
            canSearch = true
        )
    )

    object Playlists : Screen(R.string.title_playlists, R.drawable.ic_queue_music, "playlists",
        AppBarState(
            navigationIcon = R.drawable.ic_search,
            canSearch = true
        )
    )
}
