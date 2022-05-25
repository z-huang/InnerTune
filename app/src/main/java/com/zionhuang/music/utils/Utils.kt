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

fun <T> logTimeMillis(tag: String, msg: String, block: () -> T): T {
    if (!BuildConfig.DEBUG) return block()
    var result: T
    val duration = measureTimeMillis {
        result = block()
    }
    Log.d(tag, msg.format(duration))
    return result
}

fun md5(str: String): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(str.toByteArray())).toString(16).padStart(32, '0')
}

fun <T : Any, VH : RecyclerView.ViewHolder> PagingDataAdapter<T, VH>.bindLoadStateLayout(binding: LayoutLoadStateBinding) {
    addLoadStateListener { loadState ->
        binding.progressBar.isVisible = loadState.refresh is LoadState.Loading
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
