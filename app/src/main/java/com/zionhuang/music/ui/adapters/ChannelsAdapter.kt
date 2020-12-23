package com.zionhuang.music.ui.adapters

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.music.R
import com.zionhuang.music.databinding.ItemChannelBinding
import com.zionhuang.music.db.entities.ChannelEntity
import com.zionhuang.music.extensions.inflateWithBinding

class ChannelsAdapter : PagingDataAdapter<ChannelEntity, ChannelsAdapter.ChannelViewHolder>(ItemComparator()) {
    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder =
            ChannelViewHolder(parent.inflateWithBinding(R.layout.item_channel))

    inner class ChannelViewHolder(val binding: ItemChannelBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(channel: ChannelEntity) {
            binding.channel = channel
        }
    }

    fun getItemByPosition(position: Int): ChannelEntity? = getItem(position)

    internal class ItemComparator : DiffUtil.ItemCallback<ChannelEntity>() {
        override fun areItemsTheSame(oldItem: ChannelEntity, newItem: ChannelEntity): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ChannelEntity, newItem: ChannelEntity): Boolean = oldItem.name == newItem.name
        override fun getChangePayload(oldItem: ChannelEntity, newItem: ChannelEntity): ChannelEntity = newItem
    }
}