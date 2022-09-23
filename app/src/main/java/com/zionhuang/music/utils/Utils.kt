package com.zionhuang.music.utils

import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.music.databinding.LayoutLoadStateBinding
import com.zionhuang.music.extensions.context
import com.zionhuang.music.models.toErrorInfo
import com.zionhuang.music.ui.activities.ErrorActivity
import java.math.BigInteger
import java.security.MessageDigest

fun md5(str: String): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(str.toByteArray())).toString(16).padStart(32, '0')
}

fun List<String?>.joinByBullet() = filterNot { it.isNullOrEmpty() }.joinToString(separator = " • ")

fun <T : Any, VH : RecyclerView.ViewHolder> PagingDataAdapter<T, VH>.bindLoadStateLayout(binding: LayoutLoadStateBinding, isSwipeRefreshing: () -> Boolean = { false }) {
    addLoadStateListener { loadState ->
        if (loadState.refresh is LoadState.Error) {
            binding.errorMsg.text = (loadState.refresh as LoadState.Error).error.localizedMessage
            binding.btnReport.setOnClickListener {
                ErrorActivity.openActivity(binding.context, (loadState.refresh as LoadState.Error).error.toErrorInfo())
            }
        }
        binding.errorMsg.isVisible = loadState.refresh is LoadState.Error
        binding.progressBar.isVisible = loadState.refresh is LoadState.Loading && !isSwipeRefreshing()
        binding.btnRetry.isVisible = loadState.refresh is LoadState.Error
        binding.btnReport.isVisible = loadState.refresh is LoadState.Error

    }
    binding.btnRetry.setOnClickListener {
        retry()
    }
}
