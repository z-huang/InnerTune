package com.zionhuang.music.ui.fragments

import android.content.Intent
import android.media.audiofx.AudioEffect.*
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import android.support.v4.media.session.PlaybackStateCompat.STATE_NONE
import android.support.v4.media.session.PlaybackStateCompat.STATE_STOPPED
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis
import com.zionhuang.innertube.models.BrowseEndpoint
import com.zionhuang.innertube.models.BrowseLocalArtistSongsEndpoint
import com.zionhuang.music.R
import com.zionhuang.music.constants.MediaConstants.STATE_NOT_DOWNLOADED
import com.zionhuang.music.constants.MediaSessionConstants.ACTION_TOGGLE_LIBRARY
import com.zionhuang.music.constants.MediaSessionConstants.ACTION_TOGGLE_LIKE
import com.zionhuang.music.databinding.BottomControlsSheetBinding
import com.zionhuang.music.extensions.exceptionHandler
import com.zionhuang.music.extensions.show
import com.zionhuang.music.playback.MediaSessionConnection
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.ui.activities.MainActivity
import com.zionhuang.music.ui.fragments.dialogs.ChoosePlaylistDialog
import com.zionhuang.music.ui.fragments.songs.PlaylistSongsFragmentArgs
import com.zionhuang.music.utils.NavigationEndpointHandler
import com.zionhuang.music.utils.makeTimeString
import com.zionhuang.music.viewmodels.PlaybackViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class BottomControlsFragment : Fragment() {
    private lateinit var binding: BottomControlsSheetBinding
    private val viewModel by activityViewModels<PlaybackViewModel>()
    private val mainActivity: MainActivity get() = requireActivity() as MainActivity
    private var sliderIsTracking: Boolean = false
    private val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BottomControlsSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        // Marquee
        binding.songTitle.isSelected = true
        binding.songArtist.isSelected = true

        lifecycleScope.launch {
            viewModel.playbackState.collect { playbackState ->
                if (playbackState.state != STATE_NONE && playbackState.state != STATE_STOPPED) {
                    if (mainActivity.bottomSheetBehavior.state == STATE_HIDDEN) {
                        mainActivity.bottomSheetBehavior.state = STATE_COLLAPSED
                    }
                }
            }
        }

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
        binding.btnQueue.setOnClickListener {
            mainActivity.bottomSheetBehavior.state = STATE_COLLAPSED
            findNavController().navigate(QueueFragmentDirections.openQueueFragment())
        }
        binding.btnAddToLibrary.setOnClickListener {
            viewModel.transportControls?.sendCustomAction(ACTION_TOGGLE_LIBRARY, null)
        }
        binding.btnFavorite.setOnClickListener {
            viewModel.transportControls?.sendCustomAction(ACTION_TOGGLE_LIKE, null)
        }
        binding.btnMore.setOnClickListener {
            val mediaMetadata = MediaSessionConnection.binder?.songPlayer?.currentMediaMetadata?.value ?: return@setOnClickListener
            val song = MediaSessionConnection.binder?.songPlayer?.currentSong
            MenuBottomSheetDialogFragment
                .newInstance(R.menu.playing_song)
                .setMenuModifier {
                    findItem(R.id.action_download).isVisible = song == null || song.song.downloadState == STATE_NOT_DOWNLOADED
                    findItem(R.id.action_view_artist).isVisible = mediaMetadata.artists.isNotEmpty()
                    findItem(R.id.action_view_album).isVisible = mediaMetadata.album != null
                }
                .setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.action_equalizer -> {
                            val equalizerIntent = Intent(ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                                putExtra(EXTRA_AUDIO_SESSION, MediaSessionConnection.binder?.songPlayer?.player?.audioSessionId)
                                putExtra(EXTRA_PACKAGE_NAME, requireContext().packageName)
                                putExtra(EXTRA_CONTENT_TYPE, CONTENT_TYPE_MUSIC)
                            }
                            if (equalizerIntent.resolveActivity(requireContext().packageManager) != null) {
                                activityResultLauncher.launch(equalizerIntent)
                            }
                        }
                        R.id.action_radio -> MediaSessionConnection.binder?.songPlayer?.startRadioSeamlessly()
                        R.id.action_add_to_playlist -> {
                            val mainContent = mainActivity.binding.mainContent
                            ChoosePlaylistDialog { playlist ->
                                GlobalScope.launch(requireContext().exceptionHandler) {
                                    if (song != null) SongRepository.addToPlaylist(playlist, song)
                                    else SongRepository.addMediaItemToPlaylist(playlist, mediaMetadata)
                                    Snackbar.make(mainContent, getString(R.string.snackbar_added_to_playlist, playlist.name), BaseTransientBottomBar.LENGTH_SHORT)
                                        .setAction(R.string.snackbar_action_view) {
                                            mainActivity.currentFragment?.exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true).addTarget(R.id.fragment_content)
                                            mainActivity.currentFragment?.reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false).addTarget(R.id.fragment_content)
                                            mainActivity.currentFragment?.findNavController()?.navigate(R.id.playlistSongsFragment, PlaylistSongsFragmentArgs.Builder(playlist.id).build().toBundle())
                                        }.show()
                                }
                            }.show(childFragmentManager, null)
                        }
                        R.id.action_download -> {
                            GlobalScope.launch(requireContext().exceptionHandler) {
                                SongRepository.downloadSong(song?.song ?: SongRepository.addSong(mediaMetadata))
                            }
                        }
                        R.id.action_view_artist -> {
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
                        R.id.action_view_album -> {
                            if (mediaMetadata.album != null) {
                                NavigationEndpointHandler(mainActivity.currentFragment!!).handle(BrowseEndpoint.albumBrowseEndpoint(mediaMetadata.album.id))
                                mainActivity.collapseBottomSheet()
                            }
                        }
                        R.id.action_share -> {
                            val intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "https://music.youtube.com/watch?v=${mediaMetadata.id}")
                            }
                            startActivity(Intent.createChooser(intent, null))
                        }
                    }
                }
                .show(requireContext())


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
        }

        lifecycleScope.launch {
            viewModel.mediaMetadata.flatMapLatest { mediaMetadata ->
                SongRepository.getSongById(mediaMetadata?.getString(METADATA_KEY_MEDIA_ID)).flow
            }.collectLatest { song ->
                binding.btnFavorite.setImageResource(if (song?.song?.liked == true) R.drawable.ic_favorite else R.drawable.ic_favorite_border)
                binding.btnAddToLibrary.setImageResource(if (song != null) R.drawable.ic_library_add_check else R.drawable.ic_library_add)
            }
        }

        lifecycleScope.launch {
            viewModel.position.collect { position ->
                if (!sliderIsTracking && binding.slider.isEnabled) {
                    binding.slider.value = position.toFloat().coerceIn(binding.slider.valueFrom, binding.slider.valueTo)
                    binding.position.text = makeTimeString(position)
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
    }
}