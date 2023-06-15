package com.zionhuang.music.ui.screens

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.zionhuang.music.R

@Immutable
sealed class Screens(
    @StringRes val titleId: Int,
    @DrawableRes val iconId: Int,
    val route: String,
) {
    object Home : Screens(R.string.home, R.drawable.ic_home, "home")
    object Songs : Screens(R.string.songs, R.drawable.ic_music_note, "songs")
    object Artists : Screens(R.string.artists, R.drawable.ic_artist, "artists")
    object Albums : Screens(R.string.albums, R.drawable.ic_album, "albums")
    object Playlists : Screens(R.string.playlists, R.drawable.ic_queue_music, "playlists")
}
