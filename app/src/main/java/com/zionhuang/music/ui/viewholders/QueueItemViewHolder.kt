package com.zionhuang.music.ui.viewholders

import android.support.v4.media.session.MediaSessionCompat
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.music.databinding.ItemQueueBinding

class QueueItemViewHolder(val binding: ItemQueueBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: MediaSessionCompat.QueueItem) {
        binding.item = item
    }
}