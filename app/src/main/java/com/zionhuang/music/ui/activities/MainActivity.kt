package com.zionhuang.music.ui.activities

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.zionhuang.music.R
import com.zionhuang.music.databinding.ActivityMainBinding
import com.zionhuang.music.extensions.getDensity
import com.zionhuang.music.extensions.replaceFragment
import com.zionhuang.music.ui.fragments.BottomControlsFragment
import com.zionhuang.music.ui.widgets.BottomSheetListener
import com.zionhuang.music.viewmodels.SongsViewModel
import com.zionhuang.music.youtube.DownloaderImpl
import kotlinx.coroutines.launch
import org.schabi.newpipe.extractor.NewPipe

class MainActivity : BindingActivity<ActivityMainBinding>() {
    private var bottomSheetCallback: BottomSheetListener? = null
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    private val songsViewModel by lazy { ViewModelProvider(this)[SongsViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUI()
        songsViewModel.deleteSong.observe(this) { song ->
            Snackbar.make(binding.root, getString(R.string.snack_bar_delete_song, song.title), Snackbar.LENGTH_LONG)
                    .setAnchorView(binding.bottomNav)
                    .setAction(R.string.snack_bar_undo) {
                        lifecycleScope.launch {
                            songsViewModel.songRepository.insert(song)
                        }
                    }
                    .show()
        }
        NewPipe.init(DownloaderImpl.init(null))
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.songsFragment,
                R.id.artistsFragment,
                R.id.channelsFragment,
                R.id.playlistsFragment,
                R.id.explorationFragment
        ))
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        binding.bottomNav.setupWithNavController(navController)
        replaceFragment(R.id.bottom_controls_container, BottomControlsFragment())
        bottomSheetBehavior = from(binding.bottomControlsSheet).apply {
            isHideable = true
            addBottomSheetCallback(BottomSheetCallback())
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        bottomSheetBehavior.setPeekHeight(binding.bottomNav.height + (54 * getDensity()).toInt(), true)
    }

    fun setBottomSheetListener(bottomSheetListener: BottomSheetListener) {
        bottomSheetCallback = bottomSheetListener
    }

    fun expandBottomSheet() {
        bottomSheetBehavior.state = STATE_EXPANDED
    }

    val tabLayout: TabLayout
        get() = binding.tabLayout

    private inner class BottomSheetCallback : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, @State newState: Int) {
            bottomSheetCallback?.onStateChanged(bottomSheet, newState)
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            binding.bottomNav.translationY = binding.bottomNav.height * slideOffset.coerceIn(0F, 1F)
            bottomSheetCallback?.onSlide(bottomSheet, slideOffset)
        }
    }

    override fun onBackPressed() {
        if (bottomSheetBehavior.state == STATE_EXPANDED) {
            bottomSheetBehavior.state = STATE_COLLAPSED
            return
        }
        super.onBackPressed()
    }

//    override fun onApplyThemeResource(theme: Theme, resId: Int, first: Boolean) {
//        super.onApplyThemeResource(theme, resId, first)
//        // TODO: make this a setting option
//        val config = resources.configuration
//        val currentNightMode = config.uiMode and Configuration.UI_MODE_NIGHT_MASK
//        if (currentNightMode == Configuration.UI_MODE_NIGHT_NO) {
//            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
//            window.statusBarColor = ContextCompat.getColor(this, R.color.white)
//        }
//    }
}