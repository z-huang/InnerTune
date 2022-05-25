package com.zionhuang.music.ui.viewholders

import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadState.Loading
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.music.databinding.ItemLoadStateBinding

class LoadStateViewHolder(
        private val binding: ItemLoadStateBinding,
        private val retry: () -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    init {
        binding.btnRetry.setOnClickListener { retry() }
    }

    fun bind(loadState: LoadState) {
        if (loadState is LoadState.Error) {
            binding.errorMsg.text = loadState.error.localizedMessage
        }
        binding.progressBar.isVisible = loadState is Loading
        binding.btnRetry.isVisible = loadState !is Loading
        binding.errorMsg.isVisible = loadState !is Loading
    }
}