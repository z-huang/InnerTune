package com.zionhuang.music.ui.viewholders

import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.music.R
import com.zionhuang.music.constants.MediaConstants.STATE_DOWNLOADED
import com.zionhuang.music.constants.MediaConstants.STATE_NOT_DOWNLOADED
import com.zionhuang.music.databinding.ItemSongBinding
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.extensions.context
import com.zionhuang.music.extensions.show
import com.zionhuang.music.models.DownloadProgress
import com.zionhuang.music.ui.fragments.MenuBottomSheetDialogFragment
import com.zionhuang.music.ui.listeners.SongPopupMenuListener

open class SongViewHolder(
    open val binding: ItemSongBinding,
    private val popupMenuListener: SongPopupMenuListener?,
) : RecyclerView.ViewHolder(binding.root) {
    val itemDetails: ItemDetailsLookup.ItemDetails<String>
        get() = object : ItemDetailsLookup.ItemDetails<String>() {
            override fun getPosition(): Int = absoluteAdapterPosition
            override fun getSelectionKey(): String? = binding.song?.id
        }

    fun bind(song: Song, selected: Boolean? = false) {
        binding.song = song
        binding.btnMoreAction.setOnClickListener {
            MenuBottomSheetDialogFragment
                .newInstance(R.menu.song)
                .setMenuModifier {
                    findItem(R.id.action_download).isVisible = song.downloadState == STATE_NOT_DOWNLOADED
                    findItem(R.id.action_remove_download).isVisible = song.downloadState == STATE_DOWNLOADED
                }
                .setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.action_edit -> popupMenuListener?.editSong(song, binding.context)
                        R.id.action_play_next -> popupMenuListener?.playNext(song, binding.context)
                        R.id.action_add_to_queue -> popupMenuListener?.addToQueue(song, binding.context)
                        R.id.action_add_to_playlist -> popupMenuListener?.addToPlaylist(song, binding.context)
                        R.id.action_download -> popupMenuListener?.downloadSong(song.id, binding.context)
                        R.id.action_remove_download -> popupMenuListener?.removeDownload(song.id, binding.context)
                        R.id.action_delete -> popupMenuListener?.deleteSongs(song)
                    }
                }
                .show(binding.context)
        }
        binding.isSelected = selected == true
        binding.executePendingBindings()
    }

    fun setProgress(progress: DownloadProgress, animate: Boolean = true) {
        binding.progressBar.run {
            max = progress.totalBytes
            setProgress(progress.currentBytes, animate)
        }
    }

    fun onSelectionChanged(selected: Boolean?) {
        binding.isSelected = selected == true
    }
}