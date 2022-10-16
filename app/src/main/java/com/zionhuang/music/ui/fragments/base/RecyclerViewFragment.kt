package com.zionhuang.music.ui.fragments.base

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.appcompat.widget.Toolbar
import androidx.core.view.doOnPreDraw
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.google.android.material.transition.MaterialFadeThrough
import com.zionhuang.music.R
import com.zionhuang.music.databinding.LayoutRecyclerviewBinding
import com.zionhuang.music.extensions.systemBarInsetsCompat

abstract class AbsRecyclerViewFragment<V : ViewBinding, T : RecyclerView.Adapter<*>> : NavigationFragment<V>() {
    abstract fun getRecyclerView(): RecyclerView
    abstract val adapter: T

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enterTransition = MaterialFadeThrough().setDuration(resources.getInteger(R.integer.motion_duration_large).toLong()).addTarget(R.id.fragment_content)
        exitTransition = MaterialFadeThrough().setDuration(resources.getInteger(R.integer.motion_duration_large).toLong()).addTarget(R.id.fragment_content)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        setupRecyclerView(getRecyclerView())
        getRecyclerView().setOnApplyWindowInsetsListener { v, insets ->
            v.updatePadding(bottom = insets.systemBarInsetsCompat.bottom)
            insets
        }
    }

    protected open fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = adapter
    }
}

abstract class RecyclerViewFragment<T : RecyclerView.Adapter<*>> : AbsRecyclerViewFragment<LayoutRecyclerviewBinding, T>() {
    override fun getViewBinding() = LayoutRecyclerviewBinding.inflate(layoutInflater)
    override fun getToolbar(): Toolbar = binding.toolbar
    override fun getRecyclerView() = binding.recyclerView
    abstract override val adapter: T

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}