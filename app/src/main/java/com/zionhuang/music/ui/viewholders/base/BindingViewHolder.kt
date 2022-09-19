package com.zionhuang.music.ui.viewholders.base

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.music.extensions.inflateWithBinding

abstract class BindingViewHolder<T : ViewDataBinding>(open val binding: T) : RecyclerView.ViewHolder(binding.root) {
    constructor(viewGroup: ViewGroup, @LayoutRes layoutId: Int) : this(viewGroup.inflateWithBinding(layoutId))
}