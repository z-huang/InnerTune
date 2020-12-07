package com.zionhuang.music.ui.fragments.songs

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.downloader.Error
import com.zionhuang.music.databinding.LayoutRecyclerviewBinding
import com.zionhuang.music.download.DownloadManager
import com.zionhuang.music.download.DownloadTask
import com.zionhuang.music.ui.adapters.DownloadAdapter
import com.zionhuang.music.ui.fragments.base.BindingFragment
import com.zionhuang.music.viewmodels.DownloadViewModel

class DownloadFragment : BindingFragment<LayoutRecyclerviewBinding>() {
    private val viewModel by viewModels<DownloadViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d("DownloadFragment", "onViewCreated")
        val downloadAdapter = DownloadAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = downloadAdapter
        }
        viewModel.setDownloadListener(object : DownloadManager.EventListener{
            override fun onTaskStarted(task: DownloadTask) {
            }

            override fun onStateUpdated(task: DownloadTask) {
            }

            override fun onDownloadCompleted(task: DownloadTask) {
            }

            override fun onDownloadError(task: DownloadTask, error: Error?) {
            }

        })
        viewModel.tasks.observe(viewLifecycleOwner, {
            downloadAdapter.setData(it)
        })
    }
}