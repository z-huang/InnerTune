package com.zionhuang.music.ui.listeners

import android.content.Context
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis
import com.zionhuang.music.R
import com.zionhuang.music.constants.MediaConstants.EXTRA_MEDIA_METADATA_ITEMS
import com.zionhuang.music.constants.MediaSessionConstants.COMMAND_ADD_TO_QUEUE
import com.zionhuang.music.constants.MediaSessionConstants.COMMAND_PLAY_NEXT
import com.zionhuang.music.db.entities.PlaylistEntity
import com.zionhuang.music.extensions.exceptionHandler
import com.zionhuang.music.extensions.toMediaItem
import com.zionhuang.music.models.MediaMetadata
import com.zionhuang.music.playback.MediaSessionConnection
import com.zionhuang.music.playback.queues.ListQueue
import com.zionhuang.music.ui.activities.MainActivity
import com.zionhuang.music.ui.fragments.dialogs.ChoosePlaylistDialog
import com.zionhuang.music.ui.fragments.songs.PlaylistSongsFragmentArgs
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

abstract class BaseMenuListener<T>(open val fragment: Fragment) {
    val context: Context get() = fragment.requireContext()
    val mainActivity: MainActivity get() = fragment.requireActivity() as MainActivity

    abstract suspend fun getMediaMetadata(items: List<T>): List<MediaMetadata>

    @OptIn(DelicateCoroutinesApi::class)
    fun playAll(queueTitle: String?, items: List<T>) {
        GlobalScope.launch(Dispatchers.Main + context.exceptionHandler) {
            MediaSessionConnection.binder?.songPlayer?.playQueue(ListQueue(
                title = queueTitle,
                items = getMediaMetadata(items).map { it.toMediaItem() }
            ))
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun playNext(items: List<T>, message: String) {
        val mainContent = mainActivity.binding.mainContent
        GlobalScope.launch(Dispatchers.Main + context.exceptionHandler) {
            MediaSessionConnection.mediaController?.sendCommand(
                COMMAND_PLAY_NEXT,
                bundleOf(EXTRA_MEDIA_METADATA_ITEMS to getMediaMetadata(items).toTypedArray()),
                null
            )
            Snackbar.make(mainContent, message, LENGTH_SHORT).show()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun addToQueue(items: List<T>, message: String) {
        val mainContent = mainActivity.binding.mainContent
        GlobalScope.launch(Dispatchers.Main + context.exceptionHandler) {
            MediaSessionConnection.mediaController?.sendCommand(
                COMMAND_ADD_TO_QUEUE,
                bundleOf(EXTRA_MEDIA_METADATA_ITEMS to getMediaMetadata(items).toTypedArray()),
                null
            )
            Snackbar.make(mainContent, message, LENGTH_SHORT).show()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun addToPlaylist(block: suspend (PlaylistEntity) -> Unit) {
        val mainContent = mainActivity.binding.mainContent
        ChoosePlaylistDialog { playlist ->
            GlobalScope.launch(context.exceptionHandler) {
                block(playlist)
                Snackbar.make(mainContent, fragment.getString(R.string.snackbar_added_to_playlist, playlist.name), LENGTH_SHORT)
                    .setAction(R.string.snackbar_action_view) {
                        fragment.exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true).addTarget(R.id.fragment_content)
                        fragment.reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false).addTarget(R.id.fragment_content)
                        fragment.findNavController().navigate(R.id.playlistSongsFragment, PlaylistSongsFragmentArgs.Builder(playlist.id).build().toBundle())
                    }.show()
            }
        }.show(fragment.childFragmentManager, null)
    }
}