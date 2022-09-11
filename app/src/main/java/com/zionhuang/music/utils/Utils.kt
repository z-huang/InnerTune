package com.zionhuang.music.utils

import android.util.Log
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.music.BuildConfig
import com.zionhuang.music.databinding.LayoutLoadStateBinding
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.system.measureTimeMillis

fun md5(str: String): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(str.toByteArray())).toString(16).padStart(32, '0')
}

fun List<String?>.joinByBullet() = filterNot { it.isNullOrEmpty() }.joinToString(separator = " • ")

fun <T : Any, VH : RecyclerView.ViewHolder> PagingDataAdapter<T, VH>.bindLoadStateLayout(binding: LayoutLoadStateBinding, isSwipeRefreshing: () -> Boolean = { false }) {
    addLoadStateListener { loadState ->
        binding.progressBar.isVisible = loadState.refresh is LoadState.Loading && !isSwipeRefreshing()
        binding.btnRetry.isVisible = loadState.refresh is LoadState.Error
        binding.errorMsg.isVisible = loadState.refresh is LoadState.Error
        if (loadState.refresh is LoadState.Error) {
            binding.errorMsg.text = (loadState.refresh as LoadState.Error).error.localizedMessage
        }
    }
    binding.btnRetry.setOnClickListener {
        retry()
    }
}
