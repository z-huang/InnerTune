package com.zionhuang.music.ui.activities

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.Intent.EXTRA_TEXT
import android.content.res.Configuration
import android.os.Bundle
import android.view.ActionMode
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.*
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomsheet.NeoBottomSheetBehavior
import com.google.android.material.bottomsheet.NeoBottomSheetBehavior.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import com.zionhuang.innertube.models.BrowseEndpoint
import com.zionhuang.innertube.models.BrowseEndpoint.Companion.artistBrowseEndpoint
import com.zionhuang.innertube.models.BrowseEndpoint.Companion.playlistBrowseEndpoint
import com.zionhuang.innertube.models.WatchEndpoint
import com.zionhuang.innertube.utils.YouTubeLinkHandler
import com.zionhuang.music.R
import com.zionhuang.music.databinding.ActivityMainBinding
import com.zionhuang.music.extensions.*
import com.zionhuang.music.playback.MediaSessionConnection
import com.zionhuang.music.playback.queues.YouTubeQueue
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.ui.activities.base.ThemedBindingActivity
import com.zionhuang.music.ui.fragments.BottomControlsFragment
import com.zionhuang.music.ui.fragments.MiniPlayerFragment
import com.zionhuang.music.ui.fragments.QueueSheetFragment
import com.zionhuang.music.ui.fragments.base.AbsRecyclerViewFragment
import com.zionhuang.music.utils.AdaptiveUtils
import com.zionhuang.music.utils.LocalizationUtils
import com.zionhuang.music.utils.NavigationEndpointHandler
import com.zionhuang.music.utils.NavigationTabHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class MainActivity : ThemedBindingActivity<ActivityMainBinding>(), NavController.OnDestinationChangedListener {
    override fun getViewBinding() = ActivityMainBinding.inflate(layoutInflater)

    private lateinit var navHostFragment: NavHostFragment
    val currentFragment: Fragment?
        get() = navHostFragment.childFragmentManager.fragments.firstOrNull()

    lateinit var bottomSheetBehavior: NeoBottomSheetBehavior<*>
    lateinit var queueSheetBehavior: NeoBottomSheetBehavior<*>

    val fab: FloatingActionButton get() = binding.fab

    private var actionMode: ActionMode? = null

    @SuppressLint("PrivateResource", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        LocalizationUtils.changeAppLanguage(this, LocalizationUtils.getAppLocale(this))
        super.onCreate(savedInstanceState)

        val defaultTabIndex = sharedPreferences.getString(getString(R.string.pref_default_open_tab), "0")!!.toInt()
        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val graph = navController.navInflater.inflate(R.navigation.main_navigation_graph)
        graph.setStartDestination(listOf(R.id.homeFragment, R.id.songsFragment, R.id.artistsFragment, R.id.albumsFragment, R.id.playlistsFragment)[defaultTabIndex])
        navController.setGraph(graph, null)
        navController.addOnDestinationChangedListener(this)

        val enabledItems = NavigationTabHelper.getEnabledItems(this)
        listOf(binding.bottomNav, binding.navigationRail).forEach {
            it.menu.forEachIndexed { index, menuItem ->
                if (!enabledItems[index]) {
                    menuItem.isVisible = false
                }
            }
        }

        binding.bottomNav.setupWithNavController(navController)
        binding.navigationRail.setupWithNavController(navController)
        binding.bottomNav.setOnItemSelectedListener { item ->
            if (item.isChecked) {
                // scroll to top
                (currentFragment as? AbsRecyclerViewFragment<*, *>)?.getRecyclerView()?.smoothScrollToPosition(0)
            } else {
                onNavDestinationSelected(item, navController)
                item.isChecked = true
            }
            true
        }
        binding.navigationRail.setOnItemSelectedListener { item ->
            if (item.isChecked) {
                // scroll to top
                (currentFragment as? AbsRecyclerViewFragment<*, *>)?.getRecyclerView()?.smoothScrollToPosition(0)
            } else {
                onNavDestinationSelected(item, navController)
                item.isChecked = true
            }
            true
        }

        binding.fab.setOnApplyWindowInsetsListener { v, insets ->
            v.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                bottomMargin = (16 * getDensity()).toInt() + insets.systemBarInsetsCompat.bottom
            }
            insets
        }
        replaceFragment(R.id.mini_player_fragment, MiniPlayerFragment())
        replaceFragment(R.id.bottom_controls_fragment, BottomControlsFragment())
        replaceFragment(R.id.queue_fragment, QueueSheetFragment())
        binding.miniPlayerFragment.background = binding.bottomNav.background
        bottomSheetBehavior = from(binding.bottomControlsSheet).apply {
            maxWidth = ViewGroup.LayoutParams.MATCH_PARENT
            isHideable = true
            state = STATE_HIDDEN
            addBottomSheetCallback(object : BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, @State newState: Int) {
                    if (newState == STATE_COLLAPSED && binding.container.paddingBottom != dip(R.dimen.bottom_controls_sheet_peek_height)) {
                        ValueAnimator.ofInt(0, dip(R.dimen.bottom_controls_sheet_peek_height)).apply {
                            duration = resources.getInteger(R.integer.motion_duration_medium).toLong()
                            interpolator = FastOutSlowInInterpolator()
                            addUpdateListener {
                                binding.container.updatePadding(bottom = it.animatedValue as Int)
                            }
                        }.start()
                    } else if (newState == STATE_HIDDEN && binding.container.paddingBottom != 0) {
                        ValueAnimator.ofInt(dip(R.dimen.bottom_controls_sheet_peek_height), 0).apply {
                            duration = resources.getInteger(R.integer.motion_duration_medium).toLong()
                            interpolator = FastOutSlowInInterpolator()
                            addUpdateListener {
                                binding.container.updatePadding(bottom = it.animatedValue as Int)
                            }
                        }.start()
                    }
                    if (newState == STATE_HIDDEN) {
                        MediaSessionConnection.mediaController?.transportControls?.stop()
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    val progress = slideOffset.coerceIn(0f, 1f)
                    binding.bottomNav.translationY = binding.bottomNav.height * progress
                    binding.miniPlayerFragment.alpha = (1 - progress * 4).coerceIn(0f, 1f) // mini player disappears after sliding 25%
                    binding.bottomControlsFragment.alpha = ((progress - 0.25f) * 4).coerceIn(0f, 1f)
                }
            })
        }
        queueSheetBehavior = from(binding.queueSheet).apply {
            maxWidth = ViewGroup.LayoutParams.MATCH_PARENT
            state = STATE_COLLAPSED
            addBottomSheetCallback(object : BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    bottomSheetBehavior.isDraggable = !(newState == STATE_EXPANDED || newState == STATE_DRAGGING)
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                }

            })
        }

        handleIntent(intent)

        lifecycleScope.launch {
            SongRepository.validateDownloads()
        }
        AdaptiveUtils.updateScreenSize(this)
        lifecycleScope.launch {
            AdaptiveUtils.screenSizeState.collectLatest {
                binding.container.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                    bottomMargin = if (it == AdaptiveUtils.ScreenSize.SMALL) resources.getDimensionPixelSize(R.dimen.m3_bottom_nav_min_height) else 0
                }
                binding.container.updatePadding(bottom = if (bottomSheetBehavior.state == STATE_HIDDEN) 0 else dip(R.dimen.bottom_controls_sheet_peek_height))
                binding.bottomNav.isVisible = it == AdaptiveUtils.ScreenSize.SMALL
                binding.navigationRail.isVisible = it != AdaptiveUtils.ScreenSize.SMALL
                bottomSheetBehavior.setPeekHeight(
                    (if (it == AdaptiveUtils.ScreenSize.SMALL) dip(R.dimen.m3_bottom_nav_min_height) else 0) + dip(R.dimen.bottom_controls_sheet_peek_height),
                    true
                )
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        AdaptiveUtils.updateScreenSize(this)
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
                NavigationEndpointHandler(it).handle(BrowseEndpoint(browseId = id))
            }
            return
        }
        YouTubeLinkHandler.getPlaylistId(url)?.let { id ->
            currentFragment?.let {
                NavigationEndpointHandler(it).handle(playlistBrowseEndpoint("VL$id"))
            }
            return
        }
        YouTubeLinkHandler.getChannelId(url)?.let { id ->
            currentFragment?.let {
                NavigationEndpointHandler(it).handle(artistBrowseEndpoint(id))
            }
            return
        }
        Snackbar.make(binding.mainContent, getString(R.string.snackbar_url_error), LENGTH_LONG).show()
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
        if (destination.id == R.id.youtubeSuggestionFragment || destination.id == R.id.localSearchFragment) {
            currentFragment?.exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).setDuration(resources.getInteger(R.integer.motion_duration_large).toLong()).addTarget(R.id.fragment_content)
            currentFragment?.reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).setDuration(resources.getInteger(R.integer.motion_duration_large).toLong()).addTarget(R.id.fragment_content)
        }
        if (destination.id in topLevelDestinations) {
            currentFragment?.reenterTransition = MaterialFadeThrough().setDuration(resources.getInteger(R.integer.motion_duration_large).toLong()).addTarget(R.id.fragment_content)
        }
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
        queueSheetBehavior.state = STATE_COLLAPSED
    }

    fun collapseBottomSheet() {
        queueSheetBehavior.state = STATE_COLLAPSED
        bottomSheetBehavior.state = STATE_COLLAPSED
    }

    override fun onBackPressed() {
        if (queueSheetBehavior.state != STATE_COLLAPSED) {
            queueSheetBehavior.state = STATE_COLLAPSED
            return
        }
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