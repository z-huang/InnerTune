package com.zionhuang.music.ui.fragments.songs

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.downloader.Error
import com.zionhuang.music.databinding.LayoutRecyclerviewBinding
import com.zionhuang.music.download.DownloadHandler
import com.zionhuang.music.download.DownloadManager
import com.zionhuang.music.download.DownloadTask
import com.zionhuang.music.ui.adapters.DownloadAdapter
import com.zionhuang.music.ui.adapters.SongsAdapter
import com.zionhuang.music.ui.fragments.base.BindingFragment
import com.zionhuang.music.viewmodels.DownloadViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DownloadFragment : BindingFragment<LayoutRecyclerviewBinding>() {
    private val viewModel by activityViewModels<DownloadViewModel>()
    private val downloadHandler = DownloadHandler()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.addDownloadListener(downloadHandler.downloadListener)
        val songsAdapter = SongsAdapter(downloadHandler)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songsAdapter
        }
        lifecycleScope.launch {
            viewModel.downloadingSongsFlow.collectLatest {
                songsAdapter.submitData(it)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.removeDownloadListener(downloadHandler.downloadListener)
    }
}