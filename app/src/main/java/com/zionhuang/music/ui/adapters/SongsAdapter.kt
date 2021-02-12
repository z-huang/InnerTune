package com.zionhuang.music.ui.adapters

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.music.R
import com.zionhuang.music.constants.Constants.HEADER_ITEM_ID
import com.zionhuang.music.constants.Constants.TYPE_HEADER
import com.zionhuang.music.constants.Constants.TYPE_ITEM
import com.zionhuang.music.constants.ORDER_ARTIST
import com.zionhuang.music.constants.ORDER_CREATE_DATE
import com.zionhuang.music.constants.ORDER_NAME
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.download.DownloadHandler
import com.zionhuang.music.download.DownloadTask.Companion.STATE_DOWNLOADING
import com.zionhuang.music.extensions.inflateWithBinding
import com.zionhuang.music.ui.listeners.SongPopupMenuListener
import com.zionhuang.music.ui.listeners.SortMenuListener
import com.zionhuang.music.ui.viewholders.SongHeaderViewHolder
import com.zionhuang.music.ui.viewholders.SongViewHolder
import me.zhanghai.android.fastscroll.PopupTextProvider
import java.text.DateFormat

class SongsAdapter(
        private val popupMenuListener: SongPopupMenuListener,
        private val downloadHandler: DownloadHandler,
) : PagingDataAdapter<Song, RecyclerView.ViewHolder>(SongItemComparator()), PopupTextProvider {
    var sortMenuListener: SortMenuListener? = null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SongViewHolder -> getItem(position)?.let {
                holder.bind(it)
                if (it.downloadState == STATE_DOWNLOADING) {
                    holder.binding.progressBar.progress = 0
                    downloadHandler.add(it.id, holder)
                }
            }
            is SongHeaderViewHolder -> holder.bind(itemCount - 1)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: List<Any>) {
        when (holder) {
            is SongViewHolder -> {
                val song = if (payloads.isEmpty()) getItem(position)!! else payloads.last() as Song
                holder.bind(song)
                if (song.downloadState == STATE_DOWNLOADING) {
                    holder.binding.progressBar.progress = 0
                    downloadHandler.add(song.id, holder)
                }
            }
            is SongHeaderViewHolder -> holder.bind(itemCount - 1)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            when (viewType) {
                TYPE_HEADER -> {
                    SongHeaderViewHolder(parent.inflateWithBinding(R.layout.item_song_header), sortMenuListener!!)
                }
                TYPE_ITEM -> SongViewHolder(parent.inflateWithBinding(R.layout.item_song), popupMenuListener)
                else -> throw IllegalArgumentException("Unexpected view type.")
            }


    fun getItemByPosition(position: Int): Song? = getItem(position)

    override fun getItemViewType(position: Int): Int =
            if (getItem(position)?.id == HEADER_ITEM_ID) {
                TYPE_HEADER
            } else {
                TYPE_ITEM
            }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is SongViewHolder) {
            holder.binding.song?.id?.let { downloadHandler.remove(it) }
        }
    }

    private val dateFormat = DateFormat.getDateInstance()

    override fun getPopupText(position: Int): String =
            if (getItemViewType(position) == TYPE_HEADER) "#"
            else when (sortMenuListener?.sortType()) {
                ORDER_CREATE_DATE -> dateFormat.format(getItem(position)!!.createDate)
                ORDER_NAME -> getItem(position)!!.title?.get(0).toString()
                ORDER_ARTIST -> getItem(position)!!.artistName
                else -> getItem(position)!!.title?.get(0).toString()
            }

    class SongItemComparator : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean = oldItem == newItem
        override fun getChangePayload(oldItem: Song, newItem: Song): Song = newItem
    }
}