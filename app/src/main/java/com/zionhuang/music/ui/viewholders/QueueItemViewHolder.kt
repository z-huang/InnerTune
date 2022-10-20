package com.zionhuang.music.ui.viewholders

import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_DURATION
import android.support.v4.media.session.MediaSessionCompat
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.music.databinding.ItemQueueBinding
import com.zionhuang.music.utils.joinByBullet
import com.zionhuang.music.utils.makeTimeString

class QueueItemViewHolder(val binding: ItemQueueBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: MediaSessionCompat.QueueItem) {
        binding.item = item
        binding.subtitle.text = listOf(item.description.subtitle.toString(), makeTimeString(item.description.extras?.getLong(METADATA_KEY_DURATION))).joinByBullet()
    }
}