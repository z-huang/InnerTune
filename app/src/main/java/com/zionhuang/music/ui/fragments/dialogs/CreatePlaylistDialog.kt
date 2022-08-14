package com.zionhuang.music.ui.fragments.dialogs

import android.app.Dialog
import android.content.DialogInterface.BUTTON_POSITIVE
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.widget.doOnTextChanged
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zionhuang.music.R
import com.zionhuang.music.constants.MediaConstants.EXTRA_BLOCK
import com.zionhuang.music.databinding.DialogSingleTextInputBinding
import com.zionhuang.music.db.entities.PlaylistEntity
import com.zionhuang.music.db.entities.PlaylistEntity.Companion.generatePlaylistId
import com.zionhuang.music.repos.SongRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

typealias OnPlaylistCreated = (PlaylistEntity) -> Unit

class CreatePlaylistDialog : AppCompatDialogFragment() {
    private lateinit var binding: DialogSingleTextInputBinding
    private var onPlaylistCreated: OnPlaylistCreated = {}

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getSerializable(EXTRA_BLOCK)?.let {
            onPlaylistCreated = it as OnPlaylistCreated
        }
    }

    private fun setupUI() {
        binding.textInput.apply {
            setHint(R.string.text_view_hint_playlist_name)
            editText?.doOnTextChanged { text, _, _, _ ->
                binding.textInput.error = if (text.isNullOrEmpty()) getString(R.string.error_playlist_name_empty) else null
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogSingleTextInputBinding.inflate(requireActivity().layoutInflater)
        setupUI()

        return MaterialAlertDialogBuilder(requireContext(), R.style.Dialog)
            .setTitle(R.string.dialog_title_create_playlist)
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

    @OptIn(DelicateCoroutinesApi::class)
    private fun onOK() {
        if (binding.textInput.editText?.text.isNullOrEmpty()) return
        val name = binding.textInput.editText?.text.toString()
        val playlist = PlaylistEntity(
            id = generatePlaylistId(),
            name = name
        )
        GlobalScope.launch {
            SongRepository.addPlaylist(playlist)
            onPlaylistCreated(playlist)
        }
        dismiss()
    }
}