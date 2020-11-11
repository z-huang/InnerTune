package com.zionhuang.music.ui.activities

import android.content.res.Configuration
import android.content.res.Resources.Theme
import android.os.Bundle
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.zionhuang.music.R
import com.zionhuang.music.databinding.ActivityMainBinding
import com.zionhuang.music.ui.fragments.BottomControlsFragment
import com.zionhuang.music.ui.widgets.BottomSheetListener
import com.zionhuang.music.utils.Utils

class MainActivity : BindingActivity<ActivityMainBinding>() {
    private var bottomSheetCallback: BottomSheetListener? = null
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<CoordinatorLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.libraryFragment, R.id.explorationFragment, R.id.settingsFragment))
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        binding.bottomNav.setupWithNavController(navController)
        Utils.replaceFragment(supportFragmentManager, R.id.bottom_controls_container, BottomControlsFragment())
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomControlsSheet).apply {
            isHideable = true
            addBottomSheetCallback(BottomSheetCallback())
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        bottomSheetBehavior.setPeekHeight((108 * Utils.getDensity(this)).toInt(), true)
    }

    fun setBottomSheetListener(bottomSheetListener: BottomSheetListener) {
        bottomSheetCallback = bottomSheetListener
    }

    private inner class BottomSheetCallback : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, @BottomSheetBehavior.State newState: Int) {
            bottomSheetCallback?.onStateChanged(bottomSheet, newState)
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            if (slideOffset >= 0) {
                binding.bottomNav.translationY = resources.getDimensionPixelOffset(R.dimen.bottom_navigation_height) * slideOffset
            }
            bottomSheetCallback?.onSlide(bottomSheet, slideOffset)
        }
    }

    override fun onBackPressed() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            return
        }
        super.onBackPressed()
    }

    override fun onApplyThemeResource(theme: Theme, resId: Int, first: Boolean) {
        super.onApplyThemeResource(theme, resId, first)
        // TODO: make this a setting option
        val config = resources.configuration
        val currentNightMode = config.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (currentNightMode == Configuration.UI_MODE_NIGHT_NO) {
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        }
    }
}