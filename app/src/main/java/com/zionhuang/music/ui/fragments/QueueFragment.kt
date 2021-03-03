package com.zionhuang.music.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialFadeThrough
import com.zionhuang.music.databinding.LayoutRecyclerviewBinding
import com.zionhuang.music.ui.adapters.MediaQueueAdapter
import com.zionhuang.music.ui.fragments.base.BindingFragment
import com.zionhuang.music.viewmodels.PlaybackViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class QueueFragment : BindingFragment<LayoutRecyclerviewBinding>() {
    private val viewModel by activityViewModels<PlaybackViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough().apply { duration = 300L }
        exitTransition = MaterialFadeThrough().apply { duration = 300L }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val queueAdapter = MediaQueueAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = queueAdapter
        }
        lifecycleScope.launch {
            viewModel.queueData.asFlow().collectLatest {
                queueAdapter.submitData(it.items)
            }
        }
    }
}