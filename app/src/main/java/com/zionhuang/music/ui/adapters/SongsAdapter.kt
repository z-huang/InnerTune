package com.zionhuang.music.ui.adapters

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.music.R
import com.zionhuang.music.databinding.ItemSongBinding
import com.zionhuang.music.db.SongEntity
import com.zionhuang.music.extensions.inflateWithBinding
import com.zionhuang.music.utils.payloadOf

class SongsAdapter : PagingDataAdapter<SongEntity, SongsAdapter.ViewHolder>(ItemComparator()) {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: List<Any?>) {
        when {
            payloads.isEmpty() -> getItem(position)?.let { holder.bind(it) }
            else -> {
                val diff = payloads.last() as List<Any?>
                diff[0]?.let { holder.binding.songTitle.text = it as String }
                diff[1]?.let { holder.binding.songArtist.text = it as String }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(parent.inflateWithBinding(R.layout.item_song))

    fun getItemByPosition(position: Int): SongEntity? = getItem(position)

    inner class ViewHolder(val binding: ItemSongBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(song: SongEntity) {
            binding.song = song
            binding.executePendingBindings()
        }
    }

    internal class ItemComparator : DiffUtil.ItemCallback<SongEntity>() {
        override fun areItemsTheSame(oldItem: SongEntity, newItem: SongEntity): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: SongEntity, newItem: SongEntity): Boolean = oldItem == newItem
        override fun getChangePayload(oldItem: SongEntity, newItem: SongEntity): List<Any?> = listOf(
                payloadOf(oldItem.title, newItem.title),
                payloadOf(oldItem.artist, newItem.artist)
        )
    }

}