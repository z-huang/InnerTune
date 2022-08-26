package com.zionhuang.music.ui.fragments.base

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewbinding.ViewBinding
import com.zionhuang.music.databinding.LayoutLoadStateBinding
import com.zionhuang.music.databinding.LayoutRecyclerviewBinding
import com.zionhuang.music.ui.adapters.LoadStateAdapter
import com.zionhuang.music.utils.bindLoadStateLayout
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

abstract class AbsPagingRecyclerViewFragment<V : ViewBinding, A : PagingDataAdapter<*, *>> : AbsRecyclerViewFragment<V, A>() {
    open fun getLayoutLoadState(): LayoutLoadStateBinding? = null
    open fun getSwipeRefreshLayout(): SwipeRefreshLayout? = null
    val refreshable: Boolean = true

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getLayoutLoadState()?.let { loadStateBinding ->
            adapter.bindLoadStateLayout(loadStateBinding, isSwipeRefreshing = {
                getSwipeRefreshLayout()?.isRefreshing ?: false
            })
        }
        getSwipeRefreshLayout()?.let { swipeRefreshLayout ->
            swipeRefreshLayout.isEnabled = refreshable
            swipeRefreshLayout.setOnRefreshListener {
                adapter.refresh()
            }
            lifecycleScope.launch {
                adapter.loadStateFlow.collectLatest { loadStates ->
                    if (loadStates.refresh !is LoadState.Loading && swipeRefreshLayout.isRefreshing) {
                        swipeRefreshLayout.isRefreshing = false
                    }
                }
            }
        }
    }

    override fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = adapter.withLoadStateFooter(LoadStateAdapter { adapter.retry() })
    }
}

abstract class PagingRecyclerViewFragment<A : PagingDataAdapter<*, *>> : AbsPagingRecyclerViewFragment<LayoutRecyclerviewBinding, A>() {
    override fun getViewBinding() = LayoutRecyclerviewBinding.inflate(layoutInflater)
    override fun getToolbar(): Toolbar = binding.toolbar
    override fun getRecyclerView() = binding.recyclerView
    override fun getLayoutLoadState() = binding.layoutLoadState
    override fun getSwipeRefreshLayout() = binding.swipeRefresh

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}