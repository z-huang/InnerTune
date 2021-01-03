package com.zionhuang.music.ui.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.google.api.services.youtube.model.Video
import com.zionhuang.music.databinding.ItemVideoBinding

class VideoViewHolder(private val binding: ItemVideoBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(video: Video) {
        binding.video = video
    }
}