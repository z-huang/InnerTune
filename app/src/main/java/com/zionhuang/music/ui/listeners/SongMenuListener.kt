package com.zionhuang.music.ui.listeners

import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.EXTRA_TEXT
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT
import com.google.android.material.snackbar.Snackbar
import com.zionhuang.innertube.models.BrowseEndpoint.Companion.albumBrowseEndpoint
import com.zionhuang.innertube.models.BrowseEndpoint.Companion.artistBrowseEndpoint
import com.zionhuang.innertube.models.BrowseLocalArtistSongsEndpoint
import com.zionhuang.innertube.models.WatchEndpoint
import com.zionhuang.music.R
import com.zionhuang.music.constants.MediaConstants.EXTRA_SONG
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.extensions.exceptionHandler
import com.zionhuang.music.extensions.show
import com.zionhuang.music.models.MediaMetadata
import com.zionhuang.music.models.toMediaMetadata
import com.zionhuang.music.playback.MediaSessionConnection
import com.zionhuang.music.playback.queues.YouTubeQueue
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.ui.fragments.dialogs.EditSongDialog
import com.zionhuang.music.utils.NavigationEndpointHandler
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

interface ISongMenuListener {
    fun editSong(song: Song)
    fun toggleLike(song: Song)
    fun startRadio(song: Song)
    fun playNext(songs: List<Song>)
    fun addToQueue(songs: List<Song>)
    fun addToPlaylist(songs: List<Song>)
    fun download(songs: List<Song>)
    fun removeDownload(songs: List<Song>)
    fun viewArtist(song: Song)
    fun viewAlbum(song: Song)
    fun refetch(songs: List<Song>)
    fun share(song: Song)
    fun delete(songs: List<Song>)

    fun playNext(song: Song) = playNext(listOf(song))
    fun addToQueue(song: Song) = addToQueue(listOf(song))
    fun addToPlaylist(song: Song) = addToPlaylist(listOf(song))
    fun download(song: Song) = download(listOf(song))
    fun removeDownload(song: Song) = removeDownload(listOf(song))
    fun refetch(song: Song) = refetch(listOf(song))
    fun delete(song: Song) = delete(listOf(song))
}

class SongMenuListener(override val fragment: Fragment) : BaseMenuListener<Song>(fragment), ISongMenuListener {
    private val songRepository by lazy { SongRepository(fragment.requireContext()) }

    override suspend fun getMediaMetadata(items: List<Song>): List<MediaMetadata> = items.map { it.toMediaMetadata() }

    override fun editSong(song: Song) {
        EditSongDialog().apply {
            arguments = bundleOf(EXTRA_SONG to song)
        }.show(context)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun toggleLike(song: Song) {
        GlobalScope.launch(context.exceptionHandler) {
            songRepository.toggleLiked(song)
        }
    }

    override fun startRadio(song: Song) {
        MediaSessionConnection.binder?.songPlayer?.playQueue(YouTubeQueue(endpoint = WatchEndpoint(videoId = song.id)))
    }

    override fun playNext(songs: List<Song>) {
        playNext(songs, context.resources.getQuantityString(R.plurals.snackbar_song_play_next, songs.size, songs.size))
    }

    override fun addToQueue(songs: List<Song>) {
        addToQueue(songs, context.resources.getQuantityString(R.plurals.snackbar_song_added_to_queue, songs.size, songs.size))
    }

    override fun addToPlaylist(songs: List<Song>) {
        addToPlaylist { playlist ->
            songRepository.addToPlaylist(playlist, songs)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun download(songs: List<Song>) {
        GlobalScope.launch(context.exceptionHandler) {
            Snackbar.make(mainActivity.binding.mainContent, context.resources.getQuantityString(R.plurals.snackbar_download_song, songs.size, songs.size), LENGTH_SHORT).show()
            songRepository.downloadSongs(songs.map { it.song })
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun removeDownload(songs: List<Song>) {
        val mainContent = mainActivity.binding.mainContent
        GlobalScope.launch {
            songRepository.removeDownloads(songs)
            Snackbar.make(mainContent, R.string.snackbar_removed_download, LENGTH_SHORT).show()
        }
    }

    override fun viewArtist(song: Song) {
        if (song.artists.isNotEmpty()) {
            val artist = song.artists[0]
            NavigationEndpointHandler(fragment).handle(if (artist.isYouTubeArtist) {
                artistBrowseEndpoint(artist.id)
            } else {
                BrowseLocalArtistSongsEndpoint(artist.id)
            })
        }
    }

    override fun viewAlbum(song: Song) {
        if (song.song.albumId != null) {
            NavigationEndpointHandler(fragment).handle(albumBrowseEndpoint(song.song.albumId))
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun refetch(songs: List<Song>) {
        GlobalScope.launch(context.exceptionHandler) {
            songRepository.refetchSongs(songs)
        }
    }

    override fun share(song: Song) {
        val intent = Intent().apply {
            action = ACTION_SEND
            type = "text/plain"
            putExtra(EXTRA_TEXT, "https://music.youtube.com/watch?v=${song.id}")
        }
        fragment.startActivity(Intent.createChooser(intent, null))
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun delete(songs: List<Song>) {
        GlobalScope.launch {
            songRepository.deleteSongs(songs)
        }
    }
}