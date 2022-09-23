package com.zionhuang.music.ui.viewholders

import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadState.Loading
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.music.databinding.LayoutLoadStateBinding
import com.zionhuang.music.extensions.context
import com.zionhuang.music.models.toErrorInfo
import com.zionhuang.music.ui.activities.ErrorActivity

class LoadStateViewHolder(
    private val binding: LayoutLoadStateBinding,
    private val retry: () -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    init {
        binding.btnRetry.setOnClickListener { retry() }
    }

    fun bind(loadState: LoadState) {
        if (loadState is LoadState.Error) {
            binding.errorMsg.text = loadState.error.localizedMessage
            binding.btnReport.setOnClickListener {
                ErrorActivity.openActivity(binding.context, loadState.error.toErrorInfo())
            }
        }
        binding.errorMsg.isVisible = loadState is LoadState.Error
        binding.progressBar.isVisible = loadState is Loading
        binding.btnRetry.isVisible = loadState is LoadState.Error
        binding.btnReport.isVisible = loadState is LoadState.Error
    }
}