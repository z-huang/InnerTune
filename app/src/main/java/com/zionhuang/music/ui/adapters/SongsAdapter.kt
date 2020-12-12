package com.zionhuang.music.ui.adapters

import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.music.R
import com.zionhuang.music.databinding.ItemSongBinding
import com.zionhuang.music.db.entities.SongEntity
import com.zionhuang.music.download.DownloadHandler
import com.zionhuang.music.download.DownloadTask
import com.zionhuang.music.download.DownloadTask.Companion.STATE_DOWNLOADING
import com.zionhuang.music.download.DownloadTask.Companion.STATE_NOT_DOWNLOADED
import com.zionhuang.music.extensions.inflateWithBinding
import com.zionhuang.music.ui.listeners.SongPopupMenuListener

class SongsAdapter(val popupMenuListener: SongPopupMenuListener, val downloadHandler: DownloadHandler) : PagingDataAdapter<SongEntity, SongsAdapter.SongViewHolder>(ItemComparator()) {
    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int, payloads: List<Any>) {
        when {
            payloads.isEmpty() -> getItem(position)?.let { holder.bind(it) }
            else -> holder.bind(payloads.last() as SongEntity)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder =
            SongViewHolder(parent.inflateWithBinding(R.layout.item_song))

    fun getItemByPosition(position: Int): SongEntity? = getItem(position)

    inner class SongViewHolder(val binding: ItemSongBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(song: SongEntity) {
            binding.song = song
            binding.btnMoreAction.setOnClickListener { view ->
                PopupMenu(view.context, view).apply {
                    setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.action_edit -> popupMenuListener.editSong(song.id, view)
                            R.id.action_download -> popupMenuListener.downloadSong(song.id, view.context)
                            R.id.action_delete -> popupMenuListener.deleteSong(song)
                        }
                        true
                    }
                    inflate(R.menu.menu_song)
                    menu.findItem(R.id.action_download).isVisible = song.downloadState == STATE_NOT_DOWNLOADED
                    show()
                }
            }
            if (song.downloadState == STATE_DOWNLOADING) {
                binding.progressBar.progress = 0
                downloadHandler.add(song.id, this)
            }
            binding.executePendingBindings()
        }

        fun setProgress(task: DownloadTask) {
            binding.progressBar.apply {
                max = task.totalBytes.toInt()
                progress = task.currentBytes.toInt()
            }
        }
    }

    override fun onViewRecycled(holder: SongViewHolder) {
        holder.binding.song?.id?.let { downloadHandler.remove(it) }
    }

    internal class ItemComparator : DiffUtil.ItemCallback<SongEntity>() {
        override fun areItemsTheSame(oldItem: SongEntity, newItem: SongEntity): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: SongEntity, newItem: SongEntity): Boolean = oldItem == newItem
        override fun getChangePayload(oldItem: SongEntity, newItem: SongEntity): SongEntity = newItem
    }

}