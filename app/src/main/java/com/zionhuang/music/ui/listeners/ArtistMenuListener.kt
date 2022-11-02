package com.zionhuang.music.ui.listeners

import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.EXTRA_TEXT
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.zionhuang.music.R
import com.zionhuang.music.constants.MediaConstants.EXTRA_ARTIST
import com.zionhuang.music.db.entities.Artist
import com.zionhuang.music.extensions.exceptionHandler
import com.zionhuang.music.extensions.show
import com.zionhuang.music.models.MediaMetadata
import com.zionhuang.music.models.sortInfo.SongSortInfoPreference
import com.zionhuang.music.models.toMediaMetadata
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.ui.fragments.dialogs.EditArtistDialog
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

interface IArtistMenuListener {
    fun edit(artist: Artist)
    fun playNext(artists: List<Artist>)
    fun addToQueue(artists: List<Artist>)
    fun addToPlaylist(artists: List<Artist>)
    fun share(artist: Artist)
    fun refetch(artists: List<Artist>)

    fun playNext(artist: Artist) = playNext(listOf(artist))
    fun addToQueue(artist: Artist) = addToQueue(listOf(artist))
    fun addToPlaylist(artist: Artist) = addToPlaylist(listOf(artist))
    fun refetch(artist: Artist) = refetch(listOf(artist))
}

class ArtistMenuListener(override val fragment: Fragment) : BaseMenuListener<Artist>(fragment), IArtistMenuListener {
    private val songRepository by lazy { SongRepository(fragment.requireContext()) }

    override suspend fun getMediaMetadata(items: List<Artist>): List<MediaMetadata> = items.flatMap { artist ->
        songRepository.getArtistSongs(artist.id, SongSortInfoPreference).getList()
    }.map {
        it.toMediaMetadata()
    }

    override fun edit(artist: Artist) {
        EditArtistDialog().apply {
            arguments = bundleOf(EXTRA_ARTIST to artist)
        }.show(context)
    }

    override fun playNext(artists: List<Artist>) {
        playNext(artists, context.resources.getQuantityString(R.plurals.snackbar_artist_play_next, artists.size, artists.size))
    }

    override fun addToQueue(artists: List<Artist>) {
        addToQueue(artists, context.resources.getQuantityString(R.plurals.snackbar_artist_added_to_queue, artists.size, artists.size))
    }

    override fun addToPlaylist(artists: List<Artist>) {
        addToPlaylist { playlist ->
            songRepository.addToPlaylist(playlist, artists)
        }
    }

    override fun share(artist: Artist) {
        if (artist.artist.isYouTubeArtist) {
            val intent = Intent().apply {
                action = ACTION_SEND
                type = "text/plain"
                putExtra(EXTRA_TEXT, "https://music.youtube.com/channel/${artist.id}")
            }
            fragment.startActivity(Intent.createChooser(intent, null))
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun refetch(artists: List<Artist>) {
        GlobalScope.launch(context.exceptionHandler) {
            songRepository.refetchArtists(artists.map { it.artist })
        }
    }
}