package com.zionhuang.music.ui.adapters

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.music.R
import com.zionhuang.music.constants.Constants.HEADER_ITEM_ID
import com.zionhuang.music.constants.Constants.TYPE_HEADER
import com.zionhuang.music.constants.Constants.TYPE_ITEM
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.extensions.inflateWithBinding
import com.zionhuang.music.models.DownloadProgress
import com.zionhuang.music.models.base.IMutableSortInfo
import com.zionhuang.music.ui.viewholders.DraggableSongViewHolder
import com.zionhuang.music.ui.viewholders.SongViewHolder

class PlaylistSongsEditAdapter : ListAdapter<Song, RecyclerView.ViewHolder>(SongItemComparator()) {
    var sortInfo: IMutableSortInfo? = null
    var itemTouchHelper: ItemTouchHelper? = null

    private val moves: MutableList<Pair<Int, Int>> = mutableListOf()
    var onProcessMove: ((List<Pair<Int, Int>>) -> Unit)? = null

    fun moveItem(from: Int, to: Int) {
        moves.add(Pair(from, to))
        notifyItemMoved(from, to)
    }

    fun processMove() {
        onProcessMove?.let {
            it(moves.toList())
            moves.clear()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SongViewHolder -> holder.bind(getItem(position)!!, false)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: List<Any>) {
        when (holder) {
            is SongViewHolder -> {
                if (payloads.isEmpty()) {
                    onBindViewHolder(holder, position)
                } else when (val payload = payloads[0]) {
                    is Song -> holder.bind(payload)
                    is DownloadProgress -> holder.setProgress(payload)
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            TYPE_ITEM -> DraggableSongViewHolder(parent.inflateWithBinding(R.layout.item_song)).apply {
                binding.dragHandle.setOnTouchListener { _, event ->
                    if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                        itemTouchHelper?.startDrag(this)
                    }
                    true
                }
            }
            else -> throw IllegalArgumentException("Unexpected view type.")
        }

    fun getItemByPosition(position: Int): Song? = getItem(position)

    override fun getItemViewType(position: Int): Int =
        if (getItem(position)?.song?.id == HEADER_ITEM_ID) TYPE_HEADER else TYPE_ITEM

    class SongItemComparator : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean = oldItem.song.id == newItem.song.id
        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean = oldItem == newItem
        override fun getChangePayload(oldItem: Song, newItem: Song): Song = newItem
    }
}