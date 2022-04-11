package com.zionhuang.music.ui.activities

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat.STATE_NONE
import android.util.Log
import android.view.ActionMode
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import com.zionhuang.music.R
import com.zionhuang.music.constants.MediaConstants.EXTRA_QUEUE_DATA
import com.zionhuang.music.constants.MediaConstants.QUEUE_YT_SINGLE
import com.zionhuang.music.databinding.ActivityMainBinding
import com.zionhuang.music.extensions.TAG
import com.zionhuang.music.extensions.getDensity
import com.zionhuang.music.extensions.replaceFragment
import com.zionhuang.music.models.QueueData
import com.zionhuang.music.ui.fragments.BottomControlsFragment
import com.zionhuang.music.ui.widgets.BottomSheetListener
import com.zionhuang.music.ui.widgets.MainFloatingActionButton
import com.zionhuang.music.viewmodels.PlaybackViewModel
import com.zionhuang.music.viewmodels.SongsViewModel
import com.zionhuang.music.youtube.NewPipeYouTubeHelper.extractVideoId
import com.zionhuang.music.youtube.NewPipeYouTubeHelper.getLinkType
import org.schabi.newpipe.extractor.StreamingService.LinkType

class MainActivity : BindingActivity<ActivityMainBinding>(), NavController.OnDestinationChangedListener {
    override fun getViewBinding() = ActivityMainBinding.inflate(layoutInflater)

    private var bottomSheetCallback: BottomSheetListener? = null
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    private val songsViewModel by lazy { ViewModelProvider(this).get(SongsViewModel::class.java) }
    private val playbackViewModel by lazy { ViewModelProvider(this).get(PlaybackViewModel::class.java) }

    val fab: MainFloatingActionButton get() = binding.fab

    private var actionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUI()
        // TODO
//        songsViewModel.deletedSongs.observe(this) { songs ->
//            Snackbar.make(binding.root, resources.getQuantityString(R.plurals.snack_bar_delete_song, songs.size, songs.size), Snackbar.LENGTH_LONG)
//                .setAnchorView(binding.bottomNav)
//                .setAction(R.string.snack_bar_undo) {
//                    lifecycleScope.launch {
//                        songsViewModel.songRepository.restoreSongs(songs)
//                    }
//                }
//                .show()
//        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        when (intent?.action) {
            ACTION_VIEW -> {
                val url = intent.data.toString()
                when (getLinkType(url)) {
                    LinkType.STREAM -> {
                        val videoId = extractVideoId(url)!!
                        playbackViewModel.playMedia(this, videoId, bundleOf(
                            EXTRA_QUEUE_DATA to QueueData(QUEUE_YT_SINGLE, queueId = videoId)
                        ))
                    }
                    LinkType.CHANNEL -> {}
                    LinkType.PLAYLIST -> {}
                    LinkType.NONE -> {}
                }
            }
        }
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.songsFragment,
                R.id.artistsFragment,
                R.id.playlistsFragment,
                R.id.explorationFragment
            )
        )
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        binding.bottomNav.setupWithNavController(navController)
        navController.addOnDestinationChangedListener(this)

        playbackViewModel.playbackState.map { it.state != STATE_NONE }.observe(this) { show ->
            if (show) {
                fab.onPlayerShow()
            } else {
                fab.onPlayerHide()
            }
        }

        replaceFragment(R.id.bottom_controls_container, BottomControlsFragment())
        bottomSheetBehavior = from(binding.bottomControlsSheet).apply {
            isHideable = true
            state = STATE_HIDDEN
            addBottomSheetCallback(BottomSheetCallback())
        }
    }

    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        actionMode?.finish()
        if (destination.id == R.id.playlistsFragment) {
            binding.fab.show()
        } else if (binding.fab.isVisible) {
            binding.fab.hide()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        bottomSheetBehavior.setPeekHeight(
            binding.bottomNav.height + (54 * getDensity()).toInt(),
            true
        )
    }

    fun setBottomSheetListener(bottomSheetListener: BottomSheetListener) {
        bottomSheetCallback = bottomSheetListener
    }

    fun expandBottomSheet() {
        bottomSheetBehavior.state = STATE_EXPANDED
    }

    fun showBottomSheet(force: Boolean = false) {
        if (bottomSheetBehavior.state == STATE_HIDDEN || force) {
            bottomSheetBehavior.state = STATE_COLLAPSED
        }
    }

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

    override fun onActionModeStarted(mode: ActionMode?) {
        super.onActionModeStarted(mode)
        actionMode = mode
    }

    override fun onActionModeFinished(mode: ActionMode?) {
        super.onActionModeFinished(mode)
        actionMode = null
    }
}