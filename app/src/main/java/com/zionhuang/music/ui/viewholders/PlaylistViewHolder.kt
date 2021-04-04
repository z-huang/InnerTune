package com.zionhuang.music.ui.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.music.databinding.ItemPlaylistBinding
import com.zionhuang.music.db.entities.PlaylistEntity

class PlaylistViewHolder(val binding: ItemPlaylistBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(playlist: PlaylistEntity) {
        binding.playlist = playlist
    }
}