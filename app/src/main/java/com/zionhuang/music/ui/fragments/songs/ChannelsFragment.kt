package com.zionhuang.music.ui.fragments.songs

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.zionhuang.music.databinding.LayoutRecyclerviewBinding
import com.zionhuang.music.ui.adapters.ChannelsAdapter
import com.zionhuang.music.ui.fragments.base.BindingFragment
import com.zionhuang.music.viewmodels.SongsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChannelsFragment : BindingFragment<LayoutRecyclerviewBinding>() {
    private val songsViewModel by activityViewModels<SongsViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val artistsAdapter = ChannelsAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = artistsAdapter
        }
        lifecycleScope.launch {
            songsViewModel.allChannelsFlow.collectLatest {
                artistsAdapter.submitData(it)
            }
        }
    }
}