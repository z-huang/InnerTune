package com.zionhuang.music.ui.listeners

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.EXTRA_TEXT
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis
import com.zionhuang.innertube.models.BrowseEndpoint.Companion.albumBrowseEndpoint
import com.zionhuang.innertube.models.BrowseEndpoint.Companion.artistBrowseEndpoint
import com.zionhuang.innertube.models.WatchEndpoint
import com.zionhuang.music.R
import com.zionhuang.music.constants.MediaConstants.EXTRA_MEDIA_METADATA_ITEMS
import com.zionhuang.music.constants.MediaConstants.EXTRA_SONG
import com.zionhuang.music.constants.MediaSessionConstants.COMMAND_ADD_TO_QUEUE
import com.zionhuang.music.constants.MediaSessionConstants.COMMAND_PLAY_NEXT
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.extensions.show
import com.zionhuang.music.models.toMediaMetadata
import com.zionhuang.music.playback.MediaSessionConnection
import com.zionhuang.music.playback.queues.YouTubeQueue
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.ui.activities.MainActivity
import com.zionhuang.music.ui.fragments.dialogs.ChoosePlaylistDialog
import com.zionhuang.music.ui.fragments.dialogs.EditSongDialog
import com.zionhuang.music.ui.fragments.songs.PlaylistSongsFragmentArgs
import com.zionhuang.music.utils.NavigationEndpointHandler
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

interface ISongMenuListener {
    fun editSong(song: Song)
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

class SongMenuListener(private val fragment: Fragment) : ISongMenuListener {
    val context: Context
        get() = fragment.requireContext()

    val mainActivity: MainActivity
        get() = fragment.requireActivity() as MainActivity

    override fun editSong(song: Song) {
        EditSongDialog().apply {
            arguments = bundleOf(EXTRA_SONG to song)
        }.show(context)
    }

    override fun startRadio(song: Song) {
        MediaSessionConnection.binder?.songPlayer?.playQueue(YouTubeQueue(endpoint = WatchEndpoint(videoId = song.id)))
    }

    override fun playNext(songs: List<Song>) {
        val mainContent = mainActivity.binding.mainContent
        MediaSessionConnection.mediaController?.sendCommand(
            COMMAND_PLAY_NEXT,
            bundleOf(EXTRA_MEDIA_METADATA_ITEMS to songs.map { it.toMediaMetadata() }.toTypedArray()),
            null
        )
        Snackbar.make(mainContent, context.resources.getQuantityString(R.plurals.snackbar_song_play_next, songs.size, songs.size), LENGTH_SHORT).show()
    }

    override fun addToQueue(songs: List<Song>) {
        val mainContent = mainActivity.binding.mainContent
        MediaSessionConnection.mediaController?.sendCommand(
            COMMAND_ADD_TO_QUEUE,
            bundleOf(EXTRA_MEDIA_METADATA_ITEMS to songs.map { it.toMediaMetadata() }.toTypedArray()),
            null
        )
        Snackbar.make(mainContent, context.resources.getQuantityString(R.plurals.snackbar_song_added_to_queue, songs.size, songs.size), LENGTH_SHORT).show()
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun addToPlaylist(songs: List<Song>) {
        val mainContent = mainActivity.binding.mainContent
        ChoosePlaylistDialog { playlist ->
            GlobalScope.launch {
                SongRepository.addToPlaylist(playlist, songs)
                Snackbar.make(mainContent, fragment.getString(R.string.snackbar_added_to_playlist, playlist.name), LENGTH_SHORT)
                    .setAction(R.string.snackbar_action_view) {
                        fragment.exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true).addTarget(R.id.fragment_content)
                        fragment.reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false).addTarget(R.id.fragment_content)
                        fragment.findNavController().navigate(R.id.playlistSongsFragment, PlaylistSongsFragmentArgs.Builder(playlist.id).build().toBundle())
                    }.show()
            }
        }.show(fragment.childFragmentManager, null)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun download(songs: List<Song>) {
        GlobalScope.launch {
            Snackbar.make(mainActivity.binding.mainContent, context.resources.getQuantityString(R.plurals.snackbar_download_song, songs.size, songs.size), LENGTH_SHORT).show()
            SongRepository.downloadSongs(songs.map { it.song })
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun removeDownload(songs: List<Song>) {
        val mainContent = mainActivity.binding.mainContent
        GlobalScope.launch {
            SongRepository.removeDownloads(songs)
            Snackbar.make(mainContent, R.string.snackbar_removed_download, LENGTH_SHORT).show()
        }
    }

    override fun viewArtist(song: Song) {
        if (song.artists.isNotEmpty()) {
            NavigationEndpointHandler(fragment).handle(artistBrowseEndpoint(song.artists[0].id))
        }
    }

    override fun viewAlbum(song: Song) {
        if (song.song.albumId != null) {
            NavigationEndpointHandler(fragment).handle(albumBrowseEndpoint(song.song.albumId))
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun refetch(songs: List<Song>) {
        GlobalScope.launch {
            SongRepository.refetchSongs(songs)
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
            SongRepository.deleteSongs(songs)
        }
    }
}