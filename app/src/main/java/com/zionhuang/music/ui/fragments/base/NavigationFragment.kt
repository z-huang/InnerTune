package com.zionhuang.music.ui.fragments.base

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.viewbinding.ViewBinding
import com.zionhuang.music.R
import com.zionhuang.music.extensions.requireAppCompatActivity

abstract class NavigationFragment<T : ViewBinding> : BindingFragment<T>() {
    abstract fun getToolbar(): Toolbar

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireAppCompatActivity().setSupportActionBar(getToolbar())
        val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.homeFragment,
            R.id.songsFragment,
            R.id.artistsFragment,
            R.id.albumsFragment,
            R.id.playlistsFragment
        ))
        getToolbar().setupWithNavController(findNavController(), appBarConfiguration)
    }
}