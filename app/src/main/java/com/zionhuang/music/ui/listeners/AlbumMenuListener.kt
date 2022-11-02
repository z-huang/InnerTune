package com.zionhuang.music.ui.listeners

import android.content.Intent
import androidx.fragment.app.Fragment
import com.zionhuang.innertube.models.BrowseEndpoint.Companion.artistBrowseEndpoint
import com.zionhuang.music.R
import com.zionhuang.music.db.entities.Album
import com.zionhuang.music.extensions.exceptionHandler
import com.zionhuang.music.models.MediaMetadata
import com.zionhuang.music.models.toMediaMetadata
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.utils.NavigationEndpointHandler
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

interface IAlbumMenuListener {
    fun playNext(albums: List<Album>)
    fun addToQueue(albums: List<Album>)
    fun addToPlaylist(albums: List<Album>)
    fun viewArtist(album: Album)
    fun share(album: Album)
    fun refetch(albums: List<Album>)
    fun delete(albums: List<Album>)

    fun playNext(album: Album) = playNext(listOf(album))
    fun addToQueue(album: Album) = addToQueue(listOf(album))
    fun addToPlaylist(album: Album) = addToPlaylist(listOf(album))
    fun refetch(album: Album) = refetch(listOf(album))
    fun delete(album: Album) = delete(listOf(album))
}

class AlbumMenuListener(override val fragment: Fragment) : BaseMenuListener<Album>(fragment), IAlbumMenuListener {
    private val songRepository by lazy { SongRepository(fragment.requireContext()) }

    override suspend fun getMediaMetadata(items: List<Album>): List<MediaMetadata> = items.flatMap { album ->
        songRepository.getAlbumSongs(album.id)
    }.map {
        it.toMediaMetadata()
    }

    override fun playNext(albums: List<Album>) {
        playNext(albums, context.resources.getQuantityString(R.plurals.snackbar_album_play_next, albums.size, albums.size))
    }

    override fun addToQueue(albums: List<Album>) {
        addToQueue(albums, context.resources.getQuantityString(R.plurals.snackbar_album_added_to_queue, albums.size, albums.size))
    }

    override fun addToPlaylist(albums: List<Album>) {
        addToPlaylist { playlist ->
            songRepository.addToPlaylist(playlist, albums)
        }
    }

    override fun viewArtist(album: Album) {
        if (album.artists.isNotEmpty()) {
            NavigationEndpointHandler(fragment).handle(artistBrowseEndpoint(album.artists[0].id))
        }
    }

    override fun share(album: Album) {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://music.youtube.com/browse/${album.id}")
        }
        fragment.startActivity(Intent.createChooser(intent, null))
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun refetch(albums: List<Album>) {
        GlobalScope.launch(context.exceptionHandler) {
            songRepository.refetchAlbums(albums.map { it.album })
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun delete(albums: List<Album>) {
        GlobalScope.launch(context.exceptionHandler) {
            songRepository.deleteAlbums(albums)
        }
    }
}