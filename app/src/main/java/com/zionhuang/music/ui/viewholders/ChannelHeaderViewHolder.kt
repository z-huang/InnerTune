package com.zionhuang.music.ui.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.music.R
import com.zionhuang.music.databinding.ItemChannelHeaderBinding
import com.zionhuang.music.extensions.context

class ChannelHeaderViewHolder(val binding: ItemChannelHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(songsCount: Int) {
        binding.songsCount.text = binding.context.resources.getQuantityString(R.plurals.songs_count, songsCount, songsCount)
        binding.executePendingBindings()
    }
}