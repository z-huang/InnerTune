package com.zionhuang.music.ui.fragments.deprecated

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.zionhuang.music.databinding.LayoutRecyclerviewBinding
import com.zionhuang.music.download.DownloadHandler
import com.zionhuang.music.ui.adapters.SongsAdapter
import com.zionhuang.music.ui.fragments.base.BindingFragment
import com.zionhuang.music.viewmodels.SongsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DownloadFragment : BindingFragment<LayoutRecyclerviewBinding>() {
    private val songsViewModel by activityViewModels<SongsViewModel>()
    private val downloadHandler = DownloadHandler()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        songsViewModel.addDownloadListener(downloadHandler.downloadListener)
        val songsAdapter = SongsAdapter(songsViewModel.songPopupMenuListener, downloadHandler)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songsAdapter
        }
        lifecycleScope.launch {
            songsViewModel.downloadingSongsFlow.collectLatest {
                songsAdapter.submitData(it)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        songsViewModel.removeDownloadListener(downloadHandler.downloadListener)
    }
}