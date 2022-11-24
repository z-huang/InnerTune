package com.zionhuang.music.compose.screens

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.zionhuang.music.R

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
