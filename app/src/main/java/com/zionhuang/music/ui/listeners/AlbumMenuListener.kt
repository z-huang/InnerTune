package com.zionhuang.music.ui.listeners

import android.content.Context
import android.content.Intent
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis
import com.zionhuang.innertube.models.BrowseEndpoint.Companion.artistBrowseEndpoint
import com.zionhuang.music.R
import com.zionhuang.music.constants.MediaConstants.EXTRA_MEDIA_METADATA_ITEMS
import com.zionhuang.music.constants.MediaSessionConstants
import com.zionhuang.music.constants.MediaSessionConstants.COMMAND_PLAY_NEXT
import com.zionhuang.music.db.entities.Album
import com.zionhuang.music.extensions.exceptionHandler
import com.zionhuang.music.models.toMediaMetadata
import com.zionhuang.music.playback.MediaSessionConnection
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.ui.activities.MainActivity
import com.zionhuang.music.ui.fragments.dialogs.ChoosePlaylistDialog
import com.zionhuang.music.ui.fragments.songs.PlaylistSongsFragmentArgs
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

class AlbumMenuListener(private val fragment: Fragment) : IAlbumMenuListener {
    val context: Context
        get() = fragment.requireContext()

    val mainActivity: MainActivity
        get() = fragment.requireActivity() as MainActivity

    @OptIn(DelicateCoroutinesApi::class)
    override fun playNext(albums: List<Album>) {
        val mainContent = mainActivity.binding.mainContent
        GlobalScope.launch(context.exceptionHandler) {
            val songs = albums.flatMap { album ->
                SongRepository.getAlbumSongs(album.id)
            }
            MediaSessionConnection.mediaController?.sendCommand(
                COMMAND_PLAY_NEXT,
                bundleOf(EXTRA_MEDIA_METADATA_ITEMS to songs.map { it.toMediaMetadata() }.toTypedArray()),
                null
            )
            Snackbar.make(mainContent, context.resources.getQuantityString(R.plurals.snackbar_album_play_next, albums.size, albums.size), LENGTH_SHORT).show()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun addToQueue(albums: List<Album>) {
        val mainContent = mainActivity.binding.mainContent
        GlobalScope.launch(context.exceptionHandler) {
            val songs = albums.flatMap { album ->
                SongRepository.getAlbumSongs(album.id)
            }
            MediaSessionConnection.mediaController?.sendCommand(
                MediaSessionConstants.COMMAND_ADD_TO_QUEUE,
                bundleOf(EXTRA_MEDIA_METADATA_ITEMS to songs.map { it.toMediaMetadata() }.toTypedArray()),
                null
            )
            Snackbar.make(mainContent, context.resources.getQuantityString(R.plurals.snackbar_album_added_to_queue, albums.size, albums.size), LENGTH_SHORT).show()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun addToPlaylist(albums: List<Album>) {
        val mainContent = mainActivity.binding.mainContent
        ChoosePlaylistDialog { playlist ->
            GlobalScope.launch(context.exceptionHandler) {
                SongRepository.addToPlaylist(playlist, albums)
                Snackbar.make(mainContent, fragment.getString(R.string.snackbar_added_to_playlist, playlist.name), LENGTH_SHORT)
                    .setAction(R.string.snackbar_action_view) {
                        fragment.exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true).addTarget(R.id.fragment_content)
                        fragment.reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false).addTarget(R.id.fragment_content)
                        fragment.findNavController().navigate(R.id.playlistSongsFragment, PlaylistSongsFragmentArgs.Builder(playlist.id).build().toBundle())
                    }.show()
            }
        }.show(fragment.childFragmentManager, null)
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
            SongRepository.refetchAlbums(albums.map { it.album })
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun delete(albums: List<Album>) {
        GlobalScope.launch(context.exceptionHandler) {
            SongRepository.deleteAlbums(albums)
        }
    }
}