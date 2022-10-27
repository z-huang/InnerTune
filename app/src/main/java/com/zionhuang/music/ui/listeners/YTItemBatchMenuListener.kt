package com.zionhuang.music.ui.listeners

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.*
import com.zionhuang.music.R
import com.zionhuang.music.extensions.exceptionHandler
import com.zionhuang.music.extensions.toMediaItem
import com.zionhuang.music.playback.MediaSessionConnection
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.ui.activities.MainActivity
import com.zionhuang.music.ui.fragments.dialogs.ChoosePlaylistDialog
import com.zionhuang.music.ui.fragments.songs.PlaylistSongsFragmentArgs
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO

interface IYTItemBatchMenuListener {
    fun playNext(items: List<YTItem>)
    fun addToQueue(items: List<YTItem>)
    fun addToLibrary(items: List<YTItem>)
    fun addToPlaylist(items: List<YTItem>)
    fun download(items: List<YTItem>)
}

class YTItemBatchMenuListener(val fragment: Fragment) : IYTItemBatchMenuListener {
    private val songRepository by lazy { SongRepository(fragment.requireContext()) }
    val context: Context
        get() = fragment.requireContext()

    val mainActivity: MainActivity
        get() = fragment.requireActivity() as MainActivity

    @OptIn(DelicateCoroutinesApi::class)
    override fun playNext(items: List<YTItem>) {
        val mainContent = mainActivity.binding.mainContent
        GlobalScope.launch(Dispatchers.Main + context.exceptionHandler) {
            MediaSessionConnection.binder?.songPlayer?.playNext(items.flatMap { item ->
                when (item) {
                    is SongItem -> listOf(item.toMediaItem())
                    is AlbumItem -> withContext(IO) {
                        YouTube.browse(BrowseEndpoint(browseId = "VL" + item.playlistId)).getOrThrow().items.filterIsInstance<SongItem>().map { it.toMediaItem() }
                        // consider refetch by [YouTube.getQueue] if needed
                    }
                    is PlaylistItem -> withContext(IO) {
                        YouTube.getQueue(playlistId = item.id).getOrThrow().map { it.toMediaItem() }
                    }
                    is ArtistItem -> emptyList()
                }
            })
            Snackbar.make(
                mainContent,
                when {
                    items.all { it is SongItem } -> context.resources.getQuantityString(R.plurals.snackbar_song_play_next, items.size, items.size)
                    items.all { it is AlbumItem } -> context.resources.getQuantityString(R.plurals.snackbar_album_play_next, items.size, items.size)
                    items.all { it is PlaylistItem } -> context.resources.getQuantityString(R.plurals.snackbar_playlist_play_next, items.size, items.size)
                    else -> context.getString(R.string.snackbar_play_next)
                },
                LENGTH_SHORT
            ).show()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun addToQueue(items: List<YTItem>) {
        val mainContent = mainActivity.binding.mainContent
        GlobalScope.launch(Dispatchers.Main + context.exceptionHandler) {
            MediaSessionConnection.binder?.songPlayer?.addToQueue(items.flatMap { item ->
                when (item) {
                    is SongItem -> listOf(item.toMediaItem())
                    is AlbumItem -> withContext(IO) {
                        YouTube.browse(BrowseEndpoint(browseId = "VL" + item.playlistId)).getOrThrow().items.filterIsInstance<SongItem>().map { it.toMediaItem() }
                        // consider refetch by [YouTube.getQueue] if needed
                    }
                    is PlaylistItem -> withContext(IO) {
                        YouTube.getQueue(playlistId = item.id).getOrThrow().map { it.toMediaItem() }
                    }
                    is ArtistItem -> emptyList()
                }
            })
            Snackbar.make(
                mainContent,
                when {
                    items.all { it is SongItem } -> context.resources.getQuantityString(R.plurals.snackbar_song_added_to_queue, items.size, items.size)
                    items.all { it is AlbumItem } -> context.resources.getQuantityString(R.plurals.snackbar_album_added_to_queue, items.size, items.size)
                    items.all { it is PlaylistItem } -> context.resources.getQuantityString(R.plurals.snackbar_playlist_added_to_queue, items.size, items.size)
                    else -> context.getString(R.string.snackbar_added_to_queue)
                },
                LENGTH_SHORT
            ).show()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun addToLibrary(items: List<YTItem>) {
        val mainContent = mainActivity.binding.mainContent
        GlobalScope.launch(context.exceptionHandler) {
            songRepository.safeAddSongs(items.filterIsInstance<SongItem>())
            songRepository.addAlbums(items.filterIsInstance<AlbumItem>())
            songRepository.addPlaylists(items.filterIsInstance<PlaylistItem>())
            Snackbar.make(mainContent, R.string.snackbar_added_to_library, LENGTH_SHORT).show()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun addToPlaylist(items: List<YTItem>) {
        val mainContent = mainActivity.binding.mainContent
        ChoosePlaylistDialog { playlist ->
            GlobalScope.launch(context.exceptionHandler) {
                songRepository.addYouTubeItemsToPlaylist(playlist, items)
                Snackbar.make(mainContent, fragment.getString(R.string.snackbar_added_to_playlist, playlist.name), LENGTH_SHORT)
                    .setAction(R.string.snackbar_action_view) {
                        fragment.exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true).addTarget(R.id.fragment_content)
                        fragment.reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false).addTarget(R.id.fragment_content)
                        fragment.findNavController().navigate(R.id.playlistSongsFragment, PlaylistSongsFragmentArgs.Builder(playlist.id).build().toBundle())
                    }.show()
            }
        }.show(fragment.childFragmentManager, null)
    }

    override fun download(items: List<YTItem>) {
        TODO()
    }
}