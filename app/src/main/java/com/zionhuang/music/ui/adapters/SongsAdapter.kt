package com.zionhuang.music.ui.adapters

import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.music.R
import com.zionhuang.music.constants.ORDER_ARTIST
import com.zionhuang.music.constants.ORDER_CREATE_DATE
import com.zionhuang.music.constants.ORDER_NAME
import com.zionhuang.music.databinding.ItemSongBinding
import com.zionhuang.music.databinding.ItemSongHeaderBinding
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.download.DownloadHandler
import com.zionhuang.music.download.DownloadTask
import com.zionhuang.music.download.DownloadTask.Companion.STATE_DOWNLOADING
import com.zionhuang.music.download.DownloadTask.Companion.STATE_NOT_DOWNLOADED
import com.zionhuang.music.extensions.inflateWithBinding
import com.zionhuang.music.ui.listeners.SongPopupMenuListener
import com.zionhuang.music.ui.listeners.SortMenuListener

private const val TYPE_HEADER = 0
private const val TYPE_ITEM = 1

class SongsAdapter(
        val popupMenuListener: SongPopupMenuListener,
        val downloadHandler: DownloadHandler,
) : PagingDataAdapter<Song, RecyclerView.ViewHolder>(ItemComparator()) {
    var sortMenuListener: SortMenuListener? = null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SongViewHolder -> getItem(position)?.let { holder.bind(it) }
            is SongHeaderViewHolder -> holder.bind(itemCount)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: List<Any>) {
        when (holder) {
            is SongViewHolder -> when {
                payloads.isEmpty() -> getItem(position)?.let { holder.bind(it) }
                else -> holder.bind(payloads.last() as Song)
            }
            is SongHeaderViewHolder -> holder.bind(itemCount)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            when (viewType) {
                TYPE_HEADER -> {
                    SongHeaderViewHolder(parent.inflateWithBinding(R.layout.item_song_header))
                }
                TYPE_ITEM -> SongViewHolder(parent.inflateWithBinding(R.layout.item_song))
                else -> throw IllegalArgumentException("Unexpected view type.")
            }


    fun getItemByPosition(position: Int): Song? = getItem(position)

    override fun getItemViewType(position: Int): Int =
            if (getItem(position)!!.id == "\$HEADER$") {
                TYPE_HEADER
            } else {
                TYPE_ITEM
            }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is SongViewHolder) {
            holder.binding.song?.id?.let { downloadHandler.remove(it) }
        }
    }

    inner class SongViewHolder(val binding: ItemSongBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(song: Song) {
            binding.song = song
            binding.btnMoreAction.setOnClickListener { view ->
                PopupMenu(view.context, view).apply {
                    inflate(R.menu.menu_song)
                    setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.action_edit -> popupMenuListener.editSong(song.id, view)
                            R.id.action_download -> popupMenuListener.downloadSong(song.id, view.context)
                            R.id.action_delete -> popupMenuListener.deleteSong(song.id)
                        }
                        true
                    }
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

    inner class SongHeaderViewHolder(val binding: ItemSongHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(songsCount: Int) {
            binding.allSongsCount.text = binding.root.context.resources.getQuantityString(R.plurals.channel_songs_count, songsCount, songsCount)
            binding.sortMenu.setOnClickListener { view ->
                PopupMenu(view.context, view).apply {
                    inflate(R.menu.sort_song)
                    setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.sort_by_create_date -> sortMenuListener?.sortByCreateDate()
                            R.id.sort_by_name -> sortMenuListener?.sortByName()
                            R.id.sort_by_artist -> sortMenuListener?.sortByArtist()
                        }
                        true
                    }
                    when (sortMenuListener?.sortType()) {
                        ORDER_CREATE_DATE -> R.id.sort_by_create_date
                        ORDER_NAME -> R.id.sort_by_name
                        ORDER_ARTIST -> R.id.sort_by_artist
                        else -> null
                    }?.let {
                        menu.findItem(it)?.isChecked = true
                    }
                    show()
                }
            }
        }
    }

    internal class ItemComparator : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean = oldItem == newItem
        override fun getChangePayload(oldItem: Song, newItem: Song): Song = newItem
    }
}