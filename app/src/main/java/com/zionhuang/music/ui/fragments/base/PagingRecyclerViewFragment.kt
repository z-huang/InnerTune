package com.zionhuang.music.ui.fragments.base

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.music.ui.adapters.LoadStateAdapter
import com.zionhuang.music.utils.bindLoadStateLayout

abstract class PagingRecyclerViewFragment<A : PagingDataAdapter<*, *>> : RecyclerViewFragment<A>() {
    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter.bindLoadStateLayout(binding.layoutLoadState)
    }

    override fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = adapter.withLoadStateFooter(LoadStateAdapter { adapter.retry() })
    }
}