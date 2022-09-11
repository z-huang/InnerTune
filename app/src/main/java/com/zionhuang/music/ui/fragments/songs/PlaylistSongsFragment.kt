package com.zionhuang.music.ui.fragments.songs

import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.MaterialSharedAxis
import com.zionhuang.music.R
import com.zionhuang.music.db.entities.LocalItem
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.extensions.addOnClickListener
import com.zionhuang.music.extensions.requireAppCompatActivity
import com.zionhuang.music.extensions.toMediaItem
import com.zionhuang.music.playback.queues.ListQueue
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.ui.adapters.DraggableLocalItemAdapter
import com.zionhuang.music.ui.fragments.base.RecyclerViewFragment
import com.zionhuang.music.ui.listeners.SongMenuListener
import com.zionhuang.music.ui.viewholders.SongViewHolder
import com.zionhuang.music.viewmodels.PlaybackViewModel
import com.zionhuang.music.viewmodels.SongsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PlaylistSongsFragment : RecyclerViewFragment<DraggableLocalItemAdapter>() {
    private val args: PlaylistSongsFragmentArgs by navArgs()
    private val playlistId by lazy { args.playlistId }

    private val playbackViewModel by activityViewModels<PlaybackViewModel>()
    private val songsViewModel by activityViewModels<SongsViewModel>()
    override val adapter = DraggableLocalItemAdapter().apply {
        songMenuListener = SongMenuListener(this@PlaylistSongsFragment)
        isDraggable = true
    }

    private var move: Pair<Int, Int>? = null
    private val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(UP or DOWN, LEFT or RIGHT) {
        private val elevation by lazy { requireContext().resources.getDimension(R.dimen.drag_item_elevation) }

        override fun isLongPressDragEnabled(): Boolean = false

        override fun canDropOver(recyclerView: RecyclerView, current: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) =
            current is SongViewHolder && target is SongViewHolder

        override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            if (isCurrentlyActive) {
                ViewCompat.setElevation(viewHolder.itemView, elevation)
            }
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            ViewCompat.setElevation(viewHolder.itemView, 0f)
            lifecycleScope.launch {
                move?.let {
                    SongRepository.movePlaylistItems(playlistId, it.first, it.second)
                    move = null
                }
            }
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            val from = viewHolder.absoluteAdapterPosition - 1
            val to = target.absoluteAdapterPosition - 1
            adapter.notifyItemMoved(from + 1, to + 1)
            move = Pair(move?.first ?: from, to)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.absoluteAdapterPosition - 1
            lifecycleScope.launch {
                SongRepository.removeSongFromPlaylist(playlistId, position)
            }
        }
    })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true).addTarget(R.id.fragment_content)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false).addTarget(R.id.fragment_content)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            itemTouchHelper.attachToRecyclerView(this)
        }
        binding.recyclerView.addOnClickListener { position, _ ->
            if (adapter.currentList[position] !is LocalItem) return@addOnClickListener

            playbackViewModel.playQueue(requireActivity(), ListQueue(
                items = adapter.currentList.filterIsInstance<Song>().map { it.toMediaItem() },
                startIndex = position - 1
            ))
        }
        adapter.itemTouchHelper = itemTouchHelper
        adapter.onShuffle = {
            playbackViewModel.playQueue(requireActivity(), ListQueue(
                items = adapter.currentList.filterIsInstance<Song>().shuffled().map { it.toMediaItem() }
            ))
        }

        lifecycleScope.launch {
            requireAppCompatActivity().title = SongRepository.getPlaylistById(playlistId).playlist.name
            songsViewModel.getPlaylistSongsAsFlow(playlistId).collectLatest {
                adapter.submitList(it, animation = false)
            }
        }
    }
}