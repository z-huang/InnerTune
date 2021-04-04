package com.zionhuang.music.ui.fragments.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.zionhuang.music.ui.activities.MainActivity

open class MainFragment<T : ViewBinding>(
        private val showTabs: Boolean = false,
) : BindingFragment<T>() {
    protected val activity: MainActivity
        get() = requireActivity() as MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}