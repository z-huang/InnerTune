package com.zionhuang.music.ui.adapters

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.zionhuang.music.R
import com.zionhuang.music.db.entities.ChannelEntity
import com.zionhuang.music.extensions.inflateWithBinding
import com.zionhuang.music.ui.viewholders.ChannelViewHolder

class ChannelsAdapter : PagingDataAdapter<ChannelEntity, ChannelViewHolder>(ChannelItemComparator()) {
    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder =
            ChannelViewHolder(parent.inflateWithBinding(R.layout.item_channel))

    fun getItemByPosition(position: Int): ChannelEntity? = getItem(position)

    class ChannelItemComparator : DiffUtil.ItemCallback<ChannelEntity>() {
        override fun areItemsTheSame(oldItem: ChannelEntity, newItem: ChannelEntity): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ChannelEntity, newItem: ChannelEntity): Boolean = oldItem.name == newItem.name
        override fun getChangePayload(oldItem: ChannelEntity, newItem: ChannelEntity): ChannelEntity = newItem
    }
}