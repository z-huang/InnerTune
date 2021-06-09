package com.zionhuang.music.ui.viewholders

import android.widget.PopupMenu
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.music.R
import com.zionhuang.music.constants.MediaConstants.STATE_NOT_DOWNLOADED
import com.zionhuang.music.databinding.ItemSongBinding
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.extensions.context
import com.zionhuang.music.models.DownloadProgress
import com.zionhuang.music.ui.listeners.SongPopupMenuListener

class SongViewHolder(
    val binding: ItemSongBinding,
    private val popupMenuListener: SongPopupMenuListener?,
) : RecyclerView.ViewHolder(binding.root) {
    val itemDetails: ItemDetailsLookup.ItemDetails<String>
        get() = object : ItemDetailsLookup.ItemDetails<String>() {
            override fun getPosition(): Int = absoluteAdapterPosition
            override fun getSelectionKey(): String? = binding.song?.songId
        }

    fun bind(song: Song, selected: Boolean? = false) {
        binding.song = song
        binding.btnMoreAction.setOnClickListener { view ->
            PopupMenu(view.context, view).apply {
                inflate(R.menu.song)
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.action_edit -> popupMenuListener?.editSong(song, binding.context)
                        R.id.action_play_next -> popupMenuListener?.playNext(song, binding.context)
                        R.id.action_add_to_queue -> popupMenuListener?.addToQueue(song, binding.context)
                        R.id.action_add_to_playlist -> popupMenuListener?.addToPlaylist(song, binding.context)
                        R.id.action_download -> popupMenuListener?.downloadSongs(song.songId, binding.context)
                        R.id.action_delete -> popupMenuListener?.deleteSongs(song)
                    }
                    true
                }
                menu.findItem(R.id.action_download).isVisible = song.downloadState == STATE_NOT_DOWNLOADED
                show()
            }
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