package com.zionhuang.music.ui.fragments

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE
import android.support.v4.media.session.PlaybackStateCompat.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.NeoBottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.NeoBottomSheetBehavior.STATE_HIDDEN
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.zionhuang.innertube.models.BrowseEndpoint
import com.zionhuang.innertube.models.BrowseLocalArtistSongsEndpoint
import com.zionhuang.music.R
import com.zionhuang.music.constants.MediaSessionConstants.ACTION_TOGGLE_LIKE
import com.zionhuang.music.databinding.BottomControlsSheetBinding
import com.zionhuang.music.databinding.DialogEditLyricsBinding
import com.zionhuang.music.db.entities.LyricsEntity
import com.zionhuang.music.db.entities.LyricsEntity.Companion.LYRICS_NOT_FOUND
import com.zionhuang.music.extensions.show
import com.zionhuang.music.extensions.systemBarInsetsCompat
import com.zionhuang.music.playback.MediaSessionConnection
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.ui.activities.MainActivity
import com.zionhuang.music.ui.fragments.dialogs.SearchLyricsDialog
import com.zionhuang.music.utils.NavigationEndpointHandler
import com.zionhuang.music.utils.lyrics.LyricsHelper
import com.zionhuang.music.utils.makeTimeString
import com.zionhuang.music.utils.preference.PreferenceLiveData
import com.zionhuang.music.viewmodels.PlaybackViewModel
import dev.chrisbanes.insetter.applyInsetter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class BottomControlsFragment : Fragment() {
    lateinit var binding: BottomControlsSheetBinding
    private val viewModel by activityViewModels<PlaybackViewModel>()
    private val mainActivity: MainActivity get() = requireActivity() as MainActivity
    private var sliderIsTracking: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BottomControlsSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        // Immersive layout
        binding.root.applyInsetter {
            type(navigationBars = true) {
                padding()
            }
        }
        binding.cardViewContainer.applyInsetter {
            type(statusBars = true) {
                padding()
            }
        }
        binding.lyricsView.setOnApplyWindowInsetsListener { _, insets ->
            binding.lyricsView.immersivePaddingTop = insets.systemBarInsetsCompat.top
            insets
        }
        binding.rightPart?.applyInsetter {
            type(statusBars = true, navigationBars = true) {
                padding()
            }
        }
        // Marquee
        binding.songTitle.isSelected = true
        binding.songArtist.isSelected = true
        binding.songArtist.setOnClickListener {
            val mediaMetadata = MediaSessionConnection.binder?.songPlayer?.currentMediaMetadata?.value ?: return@setOnClickListener
            if (mediaMetadata.artists.isNotEmpty()) {
                val artist = mediaMetadata.artists[0]
                NavigationEndpointHandler(mainActivity.currentFragment!!).handle(if (artist.id.startsWith("UC")) {
                    BrowseEndpoint.artistBrowseEndpoint(artist.id)
                } else {
                    BrowseLocalArtistSongsEndpoint(artist.id)
                })
                mainActivity.collapseBottomSheet()
            }
        }

        binding.btnFavorite.setOnClickListener {
            viewModel.transportControls?.sendCustomAction(ACTION_TOGGLE_LIKE, null)
        }

        binding.slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                sliderIsTracking = true
            }

            override fun onStopTrackingTouch(slider: Slider) {
                MediaSessionConnection.mediaController?.transportControls?.seekTo(slider.value.toLong())
                sliderIsTracking = false
            }
        })
        binding.slider.addOnChangeListener { _, value, _ ->
            binding.position.text = makeTimeString((value).toLong())
            if (sliderIsTracking) {
                binding.lyricsView.updateTime(value.toLong(), animate = false)
            }
        }
        binding.lyricsView.setDraggable(true) { time ->
            viewModel.mediaController?.transportControls?.seekTo(time)
            true
        }
        PreferenceLiveData(requireContext(), R.string.pref_lyrics_text_position, "1").observe(viewLifecycleOwner) {
            binding.lyricsView.setTextGravity(it.toIntOrNull() ?: 1)
        }
        binding.btnLyricsAction.setOnClickListener {
            MenuBottomSheetDialogFragment
                .newInstance(R.menu.lyrics)
                .setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_refetch -> lifecycleScope.launch {
                            MediaSessionConnection.binder?.songPlayer?.currentMediaMetadata?.value?.let { mediaMetadata ->
                                LyricsHelper.loadLyrics(requireContext(), mediaMetadata)
                            }
                        }

                        R.id.action_edit -> {
                            val mediaId = viewModel.currentLyrics.value?.id ?: return@setOnMenuItemClickListener
                            val editLyricsBinding = DialogEditLyricsBinding.inflate(layoutInflater).apply {
                                textField.editText?.setText(viewModel.currentLyrics.value?.lyrics)
                            }
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle(R.string.dialog_title_edit_lyrics)
                                .setView(editLyricsBinding.root)
                                .setPositiveButton(R.string.dialog_button_save) { _, _ ->
                                    lifecycleScope.launch {
                                        SongRepository(requireContext()).upsert(LyricsEntity(
                                            mediaId, editLyricsBinding.textField.editText?.text.toString()
                                        ))
                                    }
                                }
                                .show()
                        }
                        R.id.action_search -> {
                            val mediaMetadata = viewModel.mediaMetadata.value ?: return@setOnMenuItemClickListener
                            try {
                                startActivity(Intent(Intent.ACTION_WEB_SEARCH).apply {
                                    putExtra(SearchManager.QUERY, "${mediaMetadata.getString(METADATA_KEY_DISPLAY_SUBTITLE)} ${mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE)} lyrics")
                                })
                            } catch (_: Exception) {
                            }
                        }
                        R.id.action_choose -> {
                            val mediaMetadata = MediaSessionConnection.binder?.songPlayer?.currentMediaMetadata?.value ?: return@setOnMenuItemClickListener
                            SearchLyricsDialog(mediaMetadata).show(requireContext())
                        }
                    }
                }
                .show(requireContext())
        }
        lifecycleScope.launch {
            viewModel.playbackState.collect { playbackState ->
                if (playbackState.state != STATE_NONE && playbackState.state != STATE_STOPPED) {
                    if (mainActivity.bottomSheetBehavior.state == STATE_HIDDEN) {
                        mainActivity.bottomSheetBehavior.state = STATE_COLLAPSED
                    }
                }
                binding.lyricsView.isPlaying = playbackState.state == STATE_PLAYING || playbackState.state == STATE_BUFFERING
            }
        }
        lifecycleScope.launch {
            viewModel.position.collect { position ->
                if (!sliderIsTracking && binding.slider.isEnabled) {
                    binding.slider.value = position.toFloat().coerceIn(binding.slider.valueFrom, binding.slider.valueTo)
                    binding.position.text = makeTimeString(position)
                    binding.lyricsView.updateTime(position, animate = true)
                }
            }
        }
        lifecycleScope.launch {
            viewModel.duration.collect { duration ->
                if (duration <= 0) {
                    binding.slider.isEnabled = false
                    binding.duration.text = ""
                } else {
                    binding.slider.isEnabled = true
                    binding.slider.valueTo = duration.toFloat()
                    binding.duration.text = makeTimeString(duration)
                }
            }
        }
        lifecycleScope.launch {
            viewModel.currentSong.collectLatest { song ->
                binding.btnFavorite.setImageResource(if (song?.song?.liked == true) R.drawable.ic_favorite else R.drawable.ic_favorite_border)
            }
        }
        lifecycleScope.launch {
            viewModel.currentLyrics.collectLatest { lyrics ->
                if (lyrics == null) {
                    binding.lyricsView.reset()
                } else {
                    binding.lyricsView.loadLyrics(lyrics.lyrics.takeIf { it != LYRICS_NOT_FOUND } ?: getString(R.string.lyrics_not_found))
                }
            }
        }
    }
}