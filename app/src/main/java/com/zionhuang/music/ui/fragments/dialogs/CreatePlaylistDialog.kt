package com.zionhuang.music.ui.fragments.dialogs

import android.app.Dialog
import android.content.DialogInterface.BUTTON_POSITIVE
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.widget.doOnTextChanged
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zionhuang.music.R
import com.zionhuang.music.databinding.CreatePlaylistDialogBinding
import com.zionhuang.music.db.SongRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CreatePlaylistDialog : AppCompatDialogFragment() {
    private lateinit var binding: CreatePlaylistDialogBinding

    private fun setupUI() {
        binding.playlistName.editText?.doOnTextChanged { text, _, _, _ ->
            binding.playlistName.error = if (text.isNullOrEmpty()) getString(R.string.error_song_title_empty) else null
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = CreatePlaylistDialogBinding.inflate(requireActivity().layoutInflater)
        setupUI()

        return MaterialAlertDialogBuilder(requireContext(), R.style.Dialog)
                .setTitle(R.string.dialog_create_playlist_title)
                .setView(binding.root)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .apply {
                    setOnShowListener {
                        getButton(BUTTON_POSITIVE).setOnClickListener { onOK() }
                    }
                }
    }

    private fun onOK() {
        if (binding.playlistName.editText?.text.isNullOrEmpty()) return
        val name = binding.playlistName.editText?.text.toString()
        GlobalScope.launch {
            SongRepository(requireContext()).insertPlaylist(name)
        }
        dismiss()
    }
}