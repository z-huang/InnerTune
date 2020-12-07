package com.zionhuang.music.ui.fragments.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.viewbinding.ViewBinding
import com.google.android.material.tabs.TabLayout
import com.zionhuang.music.ui.activities.MainActivity

open class MainFragment<T : ViewBinding>(
        private val showTabs: Boolean = false,
) : BindingFragment<T>() {
    protected val activity: MainActivity
        get() = requireActivity() as MainActivity
    protected val tabLayout: TabLayout
        get() = activity.tabLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        tabLayout.isVisible = showTabs
        return view
    }
}