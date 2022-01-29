package com.zionhuang.music.ui.fragments.songs

import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.MaterialContainerTransform
import com.zionhuang.music.R
import com.zionhuang.music.constants.MediaConstants.EXTRA_PLAYLIST_ID
import com.zionhuang.music.constants.MediaConstants.EXTRA_QUEUE_DATA
import com.zionhuang.music.constants.MediaConstants.QUEUE_PLAYLIST
import com.zionhuang.music.databinding.LayoutRecyclerviewBinding
import com.zionhuang.music.extensions.addOnClickListener
import com.zionhuang.music.extensions.themeColor
import com.zionhuang.music.models.QueueData
import com.zionhuang.music.ui.adapters.PlaylistSongsAdapter
import com.zionhuang.music.ui.fragments.base.BindingFragment
import com.zionhuang.music.viewmodels.PlaybackViewModel
import com.zionhuang.music.viewmodels.PlaylistSongsViewModel
import com.zionhuang.music.viewmodels.SongsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PlaylistSongsFragment : BindingFragment<LayoutRecyclerviewBinding>() {
    override fun getViewBinding() = LayoutRecyclerviewBinding.inflate(layoutInflater)

    private val args: PlaylistSongsFragmentArgs by navArgs()
    private val playlistId by lazy { args.playlistId }

    private val playbackViewModel by activityViewModels<PlaybackViewModel>()
    private val songsViewModel by activityViewModels<SongsViewModel>()
    private val viewModel by viewModels<PlaylistSongsViewModel>()
    private val songsAdapter = PlaylistSongsAdapter()

    private val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        private val elevation by lazy { requireContext().resources.getDimension(R.dimen.drag_item_elevation) }

        override fun isLongPressDragEnabled(): Boolean = false

        override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            if (isCurrentlyActive) {
                ViewCompat.setElevation(viewHolder.itemView, elevation)
            }
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            ViewCompat.setElevation(viewHolder.itemView, 0f)
            songsAdapter.processMove()
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            val from = viewHolder.absoluteAdapterPosition
            val to = target.absoluteAdapterPosition
            songsAdapter.moveItem(from, to)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            viewModel.removeFromPlaylist(playlistId, songsAdapter.getItemByPosition(viewHolder.absoluteAdapterPosition)!!.idInPlaylist!!)
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nav_host_fragment
            duration = 300L
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(requireContext().themeColor(R.attr.colorSurface))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        songsAdapter.apply {
            popupMenuListener = songsViewModel.songPopupMenuListener
            downloadInfo = songsViewModel.downloadInfoLiveData
            itemTouchHelper = this@PlaylistSongsFragment.itemTouchHelper
            onProcessMove = {
                viewModel.processMove(playlistId, it)
            }
        }

        binding.recyclerView.apply {
            transitionName = getString(R.string.playlist_songs_transition_name)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songsAdapter
            itemTouchHelper.attachToRecyclerView(this)
            addOnClickListener { pos, _ ->
                playbackViewModel.playMedia(
                    requireActivity(), songsAdapter.getItemByPosition(pos)!!.songId, bundleOf(
                        EXTRA_QUEUE_DATA to QueueData(QUEUE_PLAYLIST, sortInfo = songsViewModel.sortInfo.parcelize(), extras = bundleOf(
                            EXTRA_PLAYLIST_ID to playlistId
                        ))
                    )
                )
            }
        }

        lifecycleScope.launch {
            songsViewModel.getPlaylistSongsAsFlow(playlistId).collectLatest {
                songsAdapter.submitData(it)
            }
        }

        songsViewModel.downloadInfoLiveData.observe(viewLifecycleOwner) { map ->
            map.forEach { (key, value) ->
                songsAdapter.setProgress(key, value)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.playlist_songs_fragment, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_edit -> findNavController().navigate(PlaylistSongsFragmentDirections.actionPlaylistSongsFragmentToPlaylistSongsEditFragment(playlistId))
        }
        return true
    }
}