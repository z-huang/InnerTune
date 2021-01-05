package com.zionhuang.music.ui.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.music.R
import com.zionhuang.music.databinding.ItemChannelHeaderBinding
import com.zionhuang.music.viewmodels.ChannelViewModel

class ChannelHeaderViewHolder(val binding: ItemChannelHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(viewModel: ChannelViewModel, songsCount: Int) {
        binding.viewModel = viewModel
        binding.songsCount.text = binding.root.context.resources.getQuantityString(R.plurals.songs_count, songsCount, songsCount)
        binding.executePendingBindings()
    }
}