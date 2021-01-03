package com.zionhuang.music.ui.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.music.databinding.ItemArtistBinding
import com.zionhuang.music.db.entities.ArtistEntity

class ArtistViewHolder(val binding: ItemArtistBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(artist: ArtistEntity) {
        binding.artist = artist
    }
}