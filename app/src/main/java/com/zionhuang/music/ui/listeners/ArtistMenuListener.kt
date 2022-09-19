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
import com.zionhuang.music.R
import com.zionhuang.music.constants.MediaConstants.EXTRA_ARTIST
import com.zionhuang.music.constants.MediaConstants.EXTRA_MEDIA_METADATA_ITEMS
import com.zionhuang.music.constants.MediaSessionConstants.COMMAND_ADD_TO_QUEUE
import com.zionhuang.music.constants.MediaSessionConstants.COMMAND_PLAY_NEXT
import com.zionhuang.music.db.entities.Artist
import com.zionhuang.music.extensions.show
import com.zionhuang.music.models.sortInfo.SongSortInfoPreference
import com.zionhuang.music.models.toMediaMetadata
import com.zionhuang.music.playback.MediaSessionConnection
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.ui.activities.MainActivity
import com.zionhuang.music.ui.fragments.dialogs.ChoosePlaylistDialog
import com.zionhuang.music.ui.fragments.dialogs.EditArtistDialog
import com.zionhuang.music.ui.fragments.songs.PlaylistSongsFragmentArgs
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
    fun delete(artists: List<Artist>)

    fun playNext(artist: Artist) = playNext(listOf(artist))
    fun addToQueue(artist: Artist) = addToQueue(listOf(artist))
    fun addToPlaylist(artist: Artist) = addToPlaylist(listOf(artist))
    fun refetch(artist: Artist) = refetch(listOf(artist))
    fun delete(artist: Artist) = delete(listOf(artist))
}

class ArtistMenuListener(private val fragment: Fragment) : IArtistMenuListener {
    val context: Context
        get() = fragment.requireContext()

    val mainActivity: MainActivity
        get() = fragment.requireActivity() as MainActivity

    override fun edit(artist: Artist) {
        EditArtistDialog().apply {
            arguments = bundleOf(EXTRA_ARTIST to artist)
        }.show(context)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun playNext(artists: List<Artist>) {
        val mainContent = mainActivity.binding.mainContent
        GlobalScope.launch {
            val songs = artists.flatMap { artist ->
                SongRepository.getArtistSongs(artist.id, SongSortInfoPreference).getList()
            }
            MediaSessionConnection.mediaController?.sendCommand(
                COMMAND_PLAY_NEXT,
                bundleOf(EXTRA_MEDIA_METADATA_ITEMS to songs.map { it.toMediaMetadata() }.toTypedArray()),
                null
            )
            Snackbar.make(mainContent, context.resources.getQuantityString(R.plurals.snackbar_artist_play_next, artists.size, artists.size), LENGTH_SHORT).show()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun addToQueue(artists: List<Artist>) {
        val mainContent = mainActivity.binding.mainContent
        GlobalScope.launch {
            val songs = artists.flatMap { artist ->
                SongRepository.getArtistSongs(artist.id, SongSortInfoPreference).getList()
            }
            MediaSessionConnection.mediaController?.sendCommand(
                COMMAND_ADD_TO_QUEUE,
                bundleOf(EXTRA_MEDIA_METADATA_ITEMS to songs.map { it.toMediaMetadata() }.toTypedArray()),
                null
            )
            Snackbar.make(mainContent, context.resources.getQuantityString(R.plurals.snackbar_artist_added_to_queue, artists.size, artists.size), LENGTH_SHORT).show()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun addToPlaylist(artists: List<Artist>) {
        val mainContent = mainActivity.binding.mainContent
        ChoosePlaylistDialog { playlist ->
            GlobalScope.launch {
                SongRepository.addToPlaylist(playlist, artists)
                Snackbar.make(mainContent, fragment.getString(R.string.snackbar_added_to_playlist, playlist.name), LENGTH_SHORT)
                    .setAction(R.string.snackbar_action_view) {
                        fragment.exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true).addTarget(R.id.fragment_content)
                        fragment.reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false).addTarget(R.id.fragment_content)
                        fragment.findNavController().navigate(R.id.playlistSongsFragment, PlaylistSongsFragmentArgs.Builder(playlist.id).build().toBundle())
                    }.show()
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
    override fun refetch(artists: List<Artist>) {
        GlobalScope.launch {
            artists.forEach { artist ->
                SongRepository.refetchArtist(artist.artist)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun delete(artists: List<Artist>) {
        GlobalScope.launch {
            SongRepository.deleteArtists(artists.map { it.artist })
        }
    }
}