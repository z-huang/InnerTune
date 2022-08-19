package com.zionhuang.music.ui.listeners

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.EXTRA_TEXT
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.zionhuang.music.constants.MediaConstants.EXTRA_ARTIST
import com.zionhuang.music.constants.MediaConstants.EXTRA_ITEM
import com.zionhuang.music.constants.MediaConstants.EXTRA_MEDIA_METADATA_ITEMS
import com.zionhuang.music.constants.MediaSessionConstants.COMMAND_ADD_TO_QUEUE
import com.zionhuang.music.constants.MediaSessionConstants.COMMAND_PLAY_NEXT
import com.zionhuang.music.db.entities.Artist
import com.zionhuang.music.extensions.show
import com.zionhuang.music.models.PreferenceSortInfo
import com.zionhuang.music.models.toMediaMetadata
import com.zionhuang.music.playback.MediaSessionConnection
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.ui.fragments.dialogs.ChoosePlaylistDialog
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
    fun delete(artists: List<Artist>)

    fun playNext(artist: Artist) = playNext(listOf(artist))
    fun addToQueue(artist: Artist) = addToQueue(listOf(artist))
    fun addToPlaylist(artist: Artist) = addToPlaylist(listOf(artist))
    fun delete(artist: Artist) = delete(listOf(artist))
}

class ArtistMenuListener(private val fragment: Fragment) : IArtistMenuListener {
    val context: Context
        get() = fragment.requireContext()

    override fun edit(artist: Artist) {
        EditArtistDialog().apply {
            arguments = bundleOf(EXTRA_ARTIST to artist)
        }.show(context)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun playNext(artists: List<Artist>) {
        GlobalScope.launch {
            val songs = artists.flatMap { artist ->
                SongRepository.getArtistSongs(artist.id, PreferenceSortInfo).getList()
            }
            MediaSessionConnection.mediaController?.sendCommand(
                COMMAND_PLAY_NEXT,
                bundleOf(EXTRA_MEDIA_METADATA_ITEMS to songs.map { it.toMediaMetadata() }.toTypedArray()),
                null
            )
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun addToQueue(artists: List<Artist>) {
        GlobalScope.launch {
            val songs = artists.flatMap { artist ->
                SongRepository.getArtistSongs(artist.id, PreferenceSortInfo).getList()
            }
            MediaSessionConnection.mediaController?.sendCommand(
                COMMAND_ADD_TO_QUEUE,
                bundleOf(EXTRA_MEDIA_METADATA_ITEMS to songs.map { it.toMediaMetadata() }.toTypedArray()),
                null
            )
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun addToPlaylist(artists: List<Artist>) {
        ChoosePlaylistDialog {
            GlobalScope.launch {
                SongRepository.addToPlaylist(it, artists)
            }
        }.show(fragment.childFragmentManager, null)
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
    override fun delete(artists: List<Artist>) {
        GlobalScope.launch {
            SongRepository.deleteArtists(artists.map { it.artist })
        }
    }
}