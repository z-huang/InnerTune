package com.zionhuang.music.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.zionhuang.music.R
import com.zionhuang.music.databinding.FragmentLibraryBinding
import com.zionhuang.music.ui.fragments.base.MainFragment
import com.zionhuang.music.ui.fragments.songs.ArtistsFragment
import com.zionhuang.music.ui.fragments.songs.ChannelsFragment
import com.zionhuang.music.ui.fragments.songs.DownloadFragment
import com.zionhuang.music.ui.fragments.songs.SongsFragment

class LibraryFragment : MainFragment<FragmentLibraryBinding>(showTabs = true) {
    private lateinit var tabLayoutMediator: TabLayoutMediator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
    }

    private fun setupViewPager() {
        Log.d(TAG, "setup viewpager")
        val libraryAdapter = LibraryAdapter(this).apply {
            addFragment(DownloadFragment(), R.string.library_tab_downloading)
            addFragment(SongsFragment(), R.string.library_tab_all_song)
            addFragment(ArtistsFragment(), R.string.library_tab_artists)
            addFragment(ChannelsFragment(), R.string.library_tab_channels)
        }
        binding.viewpager2.apply {
            adapter = libraryAdapter
            offscreenPageLimit = 1
            currentItem = 1
        }
        tabLayoutMediator = TabLayoutMediator(tabLayout, binding.viewpager2) { tab, position ->
            tab.setText(libraryAdapter.getTitle(position))
        }.apply { attach() }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_search_icon, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return true
    }

    private class LibraryAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        private val pages = mutableListOf<Page>()

        fun addFragment(fragment: Fragment, @StringRes titleResId: Int) {
            pages.add(Page(fragment, titleResId))
        }

        fun getTitle(index: Int): Int = pages[index].titleResId

        override fun createFragment(position: Int): Fragment = pages[position].fragment

        override fun getItemCount(): Int = pages.size

        inner class Page(val fragment: Fragment, @StringRes val titleResId: Int)
    }

    companion object {
        private const val TAG = "LibraryFragment"
    }
}