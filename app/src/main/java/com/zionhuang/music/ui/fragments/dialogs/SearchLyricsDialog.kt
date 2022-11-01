package com.zionhuang.music.ui.fragments.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.os.bundleOf
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zionhuang.music.R
import com.zionhuang.music.constants.MediaConstants.EXTRA_MEDIA_METADATA
import com.zionhuang.music.databinding.DialogSearchLyricsBinding
import com.zionhuang.music.models.MediaMetadata

class SearchLyricsDialog() : AppCompatDialogFragment() {
    private lateinit var binding: DialogSearchLyricsBinding
    private var mediaMetadata: MediaMetadata? = null

    constructor(mediaMetadata: MediaMetadata) : this() {
        arguments = bundleOf(EXTRA_MEDIA_METADATA to mediaMetadata)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaMetadata = arguments?.getParcelable(EXTRA_MEDIA_METADATA)
    }

    private fun setupUI() {
        binding.songTitle.editText?.setText(mediaMetadata?.title.orEmpty())
        binding.songArtist.editText?.setText(mediaMetadata?.artists?.joinToString { it.name }.orEmpty())
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogSearchLyricsBinding.inflate(layoutInflater)
        setupUI()

        return MaterialAlertDialogBuilder(requireContext(), R.style.Dialog)
            .setTitle(R.string.dialog_title_search_lyrics)
            .setView(binding.root)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                ChooseLyricsDialog(
                    mediaMetadata?.id,
                    binding.songTitle.editText?.text.toString(),
                    binding.songArtist.editText?.text.toString(),
                    mediaMetadata?.duration ?: -1
                ).show(parentFragmentManager, null)
                dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }
}