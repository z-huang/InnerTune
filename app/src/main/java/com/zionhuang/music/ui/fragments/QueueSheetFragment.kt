package com.zionhuang.music.ui.fragments

import android.content.Intent
import android.graphics.Canvas
import android.media.audiofx.AudioEffect.*
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_DURATION
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.NeoBottomSheetBehavior
import com.google.android.material.bottomsheet.NeoBottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.NeoBottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis
import com.zionhuang.innertube.models.BrowseEndpoint
import com.zionhuang.innertube.models.BrowseLocalArtistSongsEndpoint
import com.zionhuang.music.R
import com.zionhuang.music.constants.MediaConstants.STATE_NOT_DOWNLOADED
import com.zionhuang.music.constants.MediaSessionConstants.ACTION_TOGGLE_LIBRARY
import com.zionhuang.music.databinding.QueueSheetBinding
import com.zionhuang.music.extensions.*
import com.zionhuang.music.playback.MediaSessionConnection
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.ui.activities.MainActivity
import com.zionhuang.music.ui.adapters.QueueItemAdapter
import com.zionhuang.music.ui.fragments.dialogs.ChoosePlaylistDialog
import com.zionhuang.music.ui.fragments.dialogs.SongDetailsDialog
import com.zionhuang.music.ui.fragments.songs.PlaylistSongsFragmentArgs
import com.zionhuang.music.utils.NavigationEndpointHandler
import com.zionhuang.music.utils.joinByBullet
import com.zionhuang.music.utils.makeTimeString
import com.zionhuang.music.viewmodels.PlaybackViewModel
import dev.chrisbanes.insetter.applyInsetter
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class QueueSheetFragment : Fragment() {
    lateinit var binding: QueueSheetBinding
    private val viewModel by activityViewModels<PlaybackViewModel>()
    private val mainActivity: MainActivity get() = requireActivity() as MainActivity
    private val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    private val dragEventManager = DragEventManager()
    private val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        private val elevation by lazy { requireContext().resources.getDimension(R.dimen.drag_item_elevation) }

        override fun isLongPressDragEnabled(): Boolean = false

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)
            when (actionState) {
                ItemTouchHelper.ACTION_STATE_DRAG -> dragEventManager.postDragStart(viewHolder?.absoluteAdapterPosition)
            }
        }

        override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            if (isCurrentlyActive) {
                ViewCompat.setElevation(viewHolder.itemView, elevation)
            }
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            ViewCompat.setElevation(viewHolder.itemView, 0f)
            dragEventManager.postDragEnd(viewHolder.absoluteAdapterPosition)
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            val from = viewHolder.absoluteAdapterPosition
            val to = target.absoluteAdapterPosition
            adapter.moveItem(from, to)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val index = viewHolder.absoluteAdapterPosition
            adapter.removeItem(index)
            MediaSessionConnection.binder?.songPlayer?.player?.removeMediaItem(index)
        }
    })

    private val songRepository by lazy { SongRepository(requireContext()) }
    private val adapter: QueueItemAdapter = QueueItemAdapter(itemTouchHelper)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = QueueSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        binding.content.applyInsetter {
            type(statusBars = true, navigationBars = true) {
                padding()
            }
        }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            itemTouchHelper.attachToRecyclerView(this)
        }
        binding.recyclerView.addOnClickListener { pos, _ ->
            viewModel.mediaController?.seekToQueueItem(pos)
        }

        binding.btnQueue.setOnClickListener {
            mainActivity.queueSheetBehavior.state = STATE_EXPANDED
        }
        binding.btnCollapse.setOnClickListener {
            mainActivity.queueSheetBehavior.state = STATE_COLLAPSED
        }
        binding.btnLyrics.setOnClickListener {
            sharedPreferences.edit {
                putBoolean(getString(R.string.pref_show_lyrics), !sharedPreferences.getBoolean(getString(R.string.pref_show_lyrics), false))
            }
        }
        binding.btnAddToLibrary.setOnClickListener {
            viewModel.transportControls?.sendCustomAction(ACTION_TOGGLE_LIBRARY, null)
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
                        R.id.action_info -> SongDetailsDialog().show(requireContext())
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
                                    if (song != null) songRepository.addToPlaylist(playlist, song)
                                    else songRepository.addMediaItemToPlaylist(playlist, mediaMetadata)
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
                                songRepository.downloadSong(song?.song ?: songRepository.addSong(mediaMetadata))
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

        var queueSheetPrevState = mainActivity.queueSheetBehavior.state
        mainActivity.queueSheetBehavior.addBottomSheetCallback(object : NeoBottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (queueSheetPrevState == STATE_COLLAPSED) {
                    MediaSessionConnection.binder?.songPlayer?.player?.currentMediaItemIndex?.let {
                        (binding.recyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(it, 0)
                    }
                }
                queueSheetPrevState = newState
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                onSlide(slideOffset)
            }
        })
        onSlide(if (mainActivity.queueSheetBehavior.state == STATE_EXPANDED) 1f else 0f)

        lifecycleScope.launch {
            viewModel.currentSong.collectLatest { song ->
                binding.btnAddToLibrary.setImageResource(if (song != null) R.drawable.ic_library_add_check else R.drawable.ic_library_add)
            }
        }

        lifecycleScope.launch {
            viewModel.queueItems.collectLatest { items ->
                adapter.submitData(items)
                binding.queueInfo.text = listOf(
                    resources.getQuantityString(R.plurals.song_count, items.size, items.size),
                    makeTimeString(items.sumOf { item ->
                        item.description.extras?.getLong(METADATA_KEY_DURATION) ?: 0
                    })
                ).joinByBullet()
            }
        }

        dragEventManager.onDragged = { fromPos, toPos ->
            viewModel.mediaController?.moveQueueItem(fromPos, toPos)
        }
    }

    private fun onSlide(slideOffset: Float) {
        val progress = slideOffset.coerceIn(0f, 1f)
        binding.actionBar.alpha = (1 - progress * 4).coerceIn(0f, 1f)
        binding.actionBar.isVisible = 1 - progress * 4 > 0
        binding.content.alpha = ((progress - 0.25f) * 4).coerceIn(0f, 1f)
        binding.content.isVisible = progress > 0
    }

    class DragEventManager {
        private var dragFromPosition: Int? = null
        var onDragged: ((fromPos: Int, toPos: Int) -> Unit)? = null

        fun postDragStart(pos: Int?) {
            if (pos == null) return
            dragFromPosition = pos
        }

        fun postDragEnd(pos: Int?) {
            if (pos == null) return
            dragFromPosition?.let { fromPos ->
                dragFromPosition = null
                onDragged?.invoke(fromPos, pos)
            }
        }
    }
}