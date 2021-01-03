package com.zionhuang.music.ui.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.music.databinding.ItemChannelBinding
import com.zionhuang.music.db.entities.ChannelEntity

class ChannelViewHolder(val binding: ItemChannelBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(channel: ChannelEntity) {
        binding.channel = channel
    }
}