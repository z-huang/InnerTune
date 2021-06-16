package com.zionhuang.music.ui.viewholders

import androidx.core.view.isVisible
import com.zionhuang.music.databinding.ItemSongBinding

class DraggableSongViewHolder(
    override val binding: ItemSongBinding
) : SongViewHolder(binding, null) {
    init {
        binding.btnMoreAction.isVisible = false
        binding.dragHandle.isVisible = true
    }
}