package com.zionhuang.music.ui.activities

import android.animation.ValueAnimator
import android.content.Intent
import android.content.Intent.EXTRA_TEXT
import android.os.Bundle
import android.view.ActionMode
import android.view.View
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import com.zionhuang.innertube.models.BrowseEndpoint
import com.zionhuang.innertube.models.NavigationEndpoint
import com.zionhuang.innertube.models.WatchEndpoint
import com.zionhuang.innertube.utils.YouTubeLinkHandler
import com.zionhuang.music.R
import com.zionhuang.music.databinding.ActivityMainBinding
import com.zionhuang.music.extensions.dip
import com.zionhuang.music.extensions.preference
import com.zionhuang.music.extensions.replaceFragment
import com.zionhuang.music.playback.MediaSessionConnection
import com.zionhuang.music.playback.queues.YouTubeQueue
import com.zionhuang.music.ui.activities.base.ThemedBindingActivity
import com.zionhuang.music.ui.fragments.BottomControlsFragment
import com.zionhuang.music.ui.widgets.BottomSheetListener
import com.zionhuang.music.utils.NavigationEndpointHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ThemedBindingActivity<ActivityMainBinding>(), NavController.OnDestinationChangedListener {
    override fun getViewBinding() = ActivityMainBinding.inflate(layoutInflater)

    private lateinit var navHostFragment: NavHostFragment
    private val currentFragment: Fragment?
        get() = navHostFragment.childFragmentManager.fragments.firstOrNull()

    private var bottomSheetCallback: BottomSheetListener? = null
    lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    val fab: FloatingActionButton get() = binding.fab

    private var actionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUI()
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleIntent(it) }
    }

    private fun handleIntent(intent: Intent) {
        val url = (intent.data ?: intent.getStringExtra(EXTRA_TEXT))?.toString() ?: return
        YouTubeLinkHandler.getVideoId(url)?.let { id ->
            lifecycleScope.launch {
                while (!MediaSessionConnection.isConnected.value) delay(300)
                MediaSessionConnection.binder?.songPlayer?.playQueue(YouTubeQueue(WatchEndpoint(videoId = id)))
            }
            return
        }
        YouTubeLinkHandler.getBrowseId(url)?.let { id ->
            currentFragment?.let {
                NavigationEndpointHandler(it).handle(NavigationEndpoint(
                    browseEndpoint = BrowseEndpoint(browseId = id)
                ))
            }
            return
        }
        YouTubeLinkHandler.getPlaylistId(url)?.let { id ->
            currentFragment?.let {
                NavigationEndpointHandler(it).handle(NavigationEndpoint(
                    browseEndpoint = BrowseEndpoint(browseId = "VL$id")
                ))
            }
            return
        }
        YouTubeLinkHandler.getChannelId(url)?.let { id ->
            currentFragment?.let {
                NavigationEndpointHandler(it).handle(NavigationEndpoint(
                    browseEndpoint = BrowseEndpoint(browseId = id)
                ))
            }
            return
        }
        Snackbar.make(binding.mainContent, getString(R.string.snackbar_url_error), LENGTH_LONG).show()
    }

    private fun setupUI() {
        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navController.addOnDestinationChangedListener(this)
        binding.bottomNav.setupWithNavController(navController)
        binding.bottomNav.setOnItemSelectedListener { item ->
            onNavDestinationSelected(item, navController)
            item.isChecked = true
            true
        }

        replaceFragment(R.id.bottom_controls_container, BottomControlsFragment())
        bottomSheetBehavior = from(binding.bottomControlsSheet).apply {
            isHideable = true
            state = STATE_HIDDEN
            addBottomSheetCallback(BottomSheetCallback())
        }
    }

    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        val topLevelDestinations = setOf(
            R.id.homeFragment,
            R.id.songsFragment,
            R.id.artistsFragment,
            R.id.albumsFragment,
            R.id.playlistsFragment
        )
        actionMode?.finish()
        if (destination.id == R.id.playlistsFragment) {
            binding.fab.show()
        } else if (binding.fab.isVisible) {
            binding.fab.hide()
        }
        if (destination.id == R.id.youtubeSuggestionFragment) {
            currentFragment?.exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).setDuration(resources.getInteger(R.integer.motion_duration_large).toLong()).addTarget(R.id.fragment_content)
            currentFragment?.reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).setDuration(resources.getInteger(R.integer.motion_duration_large).toLong()).addTarget(R.id.fragment_content)
        }
        if (destination.id in topLevelDestinations) {
            currentFragment?.reenterTransition = MaterialFadeThrough().setDuration(resources.getInteger(R.integer.motion_duration_large).toLong()).addTarget(R.id.fragment_content)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        bottomSheetBehavior.setPeekHeight(dip(R.dimen.m3_bottom_nav_min_height) + dip(R.dimen.bottom_controls_sheet_peek_height), true)
    }

    fun setBottomSheetListener(bottomSheetListener: BottomSheetListener) {
        bottomSheetCallback = bottomSheetListener
    }

    fun showBottomSheet() {
        val expandOnPlay by preference(R.string.pref_expand_on_play, false)
        if (expandOnPlay) {
            bottomSheetBehavior.state = STATE_EXPANDED
        } else {
            if (bottomSheetBehavior.state != STATE_EXPANDED) {
                bottomSheetBehavior.state = STATE_COLLAPSED
            }
        }
    }

    private inner class BottomSheetCallback : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, @State newState: Int) {
            bottomSheetCallback?.onStateChanged(bottomSheet, newState)
            if (newState == STATE_COLLAPSED && binding.mainContent.paddingBottom != dip(R.dimen.bottom_controls_sheet_peek_height)) {
                ValueAnimator.ofInt(0, dip(R.dimen.bottom_controls_sheet_peek_height)).apply {
                    duration = resources.getInteger(R.integer.motion_duration_medium).toLong()
                    interpolator = FastOutSlowInInterpolator()
                    addUpdateListener {
                        binding.mainContent.updatePadding(bottom = it.animatedValue as Int)
                    }
                }.start()
            } else if (newState == STATE_HIDDEN && binding.mainContent.paddingBottom != 0) {
                ValueAnimator.ofInt(dip(R.dimen.bottom_controls_sheet_peek_height), 0).apply {
                    duration = resources.getInteger(R.integer.motion_duration_medium).toLong()
                    interpolator = FastOutSlowInInterpolator()
                    addUpdateListener {
                        binding.mainContent.updatePadding(bottom = it.animatedValue as Int)
                    }
                }.start()
            }
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

    override fun onActionModeStarted(mode: ActionMode?) {
        super.onActionModeStarted(mode)
        actionMode = mode
    }

    override fun onActionModeFinished(mode: ActionMode?) {
        super.onActionModeFinished(mode)
        actionMode = null
    }
}