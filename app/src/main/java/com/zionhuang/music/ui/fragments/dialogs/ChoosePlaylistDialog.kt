package com.zionhuang.music.ui.fragments.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zionhuang.music.R
import com.zionhuang.music.constants.MediaConstants.EXTRA_BLOCK
import com.zionhuang.music.databinding.DialogChoosePlaylistBinding
import com.zionhuang.music.db.entities.Playlist
import com.zionhuang.music.db.entities.PlaylistEntity
import com.zionhuang.music.extensions.addOnClickListener
import com.zionhuang.music.ui.adapters.LocalItemAdapter
import com.zionhuang.music.viewmodels.SongsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

typealias PlaylistListener = (PlaylistEntity) -> Unit

class ChoosePlaylistDialog() : AppCompatDialogFragment() {
    private lateinit var binding: DialogChoosePlaylistBinding
    private val viewModel by activityViewModels<SongsViewModel>()
    private val adapter = LocalItemAdapter().apply {
        allowMoreAction = false
    }

    private var listener: PlaylistListener? = null

    constructor(listener: PlaylistListener) : this() {
        arguments = bundleOf(EXTRA_BLOCK to listener)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listener = arguments?.getSerializable(EXTRA_BLOCK) as? PlaylistListener
    }

    private fun setupUI() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ChoosePlaylistDialog.adapter
            addOnClickListener { position, _ ->
                listener?.invoke((this@ChoosePlaylistDialog.adapter.currentList[position] as Playlist).playlist)
                dismiss()
            }
        }
        binding.createPlaylist.setOnClickListener {
            CreatePlaylistDialog(listener).show(parentFragmentManager, null)
            dismiss()
        }

        lifecycleScope.launch {
            viewModel.allPlaylistsFlow.map { pagingData ->
                pagingData.filter { item ->
                    item is Playlist && item.playlist.isLocalPlaylist
                }
            }.collectLatest {
                adapter.submitList(it)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogChoosePlaylistBinding.inflate(requireActivity().layoutInflater)
        setupUI()

        return MaterialAlertDialogBuilder(requireContext(), R.style.Dialog)
            .setTitle(R.string.dialog_title_choose_playlist)
            .setView(binding.root)
            .create()
    }
}