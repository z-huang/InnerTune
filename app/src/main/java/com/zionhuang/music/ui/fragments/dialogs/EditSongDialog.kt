package com.zionhuang.music.ui.fragments.dialogs

import android.app.Dialog
import android.content.DialogInterface.BUTTON_POSITIVE
import android.os.Bundle
import android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.widget.doOnTextChanged
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zionhuang.music.R
import com.zionhuang.music.constants.MediaConstants.EXTRA_SONG
import com.zionhuang.music.databinding.EditSongDialogBinding
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.utils.ArtistAutoCompleteAdapter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class EditSongDialog : AppCompatDialogFragment() {
    private lateinit var binding: EditSongDialogBinding
    private lateinit var song: Song

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        song = arguments?.getParcelable(EXTRA_SONG)!!
    }

    private fun setupUI() {
        (binding.songArtist.editText as? AutoCompleteTextView)?.apply {
            setAdapter(ArtistAutoCompleteAdapter(requireContext()))
        }
        binding.songTitle.editText?.doOnTextChanged { text, _, _, _ ->
            binding.songTitle.error = if (text.isNullOrEmpty()) getString(R.string.error_song_title_empty) else null
        }
        binding.songArtist.editText?.doOnTextChanged { text, _, _, _ ->
            binding.songArtist.error = if (text.isNullOrEmpty()) getString(R.string.error_song_artist_empty) else null
        }
        with(binding) {
            songTitle.editText?.setText(song.title)
            songArtist.editText?.setText(song.artistName)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = EditSongDialogBinding.inflate(requireActivity().layoutInflater)
        setupUI()

        return MaterialAlertDialogBuilder(requireContext(), R.style.Dialog)
            .setView(binding.root)
            .setTitle(R.string.dialog_title_edit_song)
            .setPositiveButton(R.string.dialog_button_save, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
            .apply {
                window!!.setSoftInputMode(SOFT_INPUT_STATE_VISIBLE)
                setOnShowListener {
                    getButton(BUTTON_POSITIVE).setOnClickListener { onSave() }
                }
            }
    }

    private fun onSave() {
        if (binding.songTitle.error != null || binding.songArtist.error != null) {
            return
        }
        val title = binding.songTitle.editText?.text.toString()
        val artistName = binding.songArtist.editText?.text.toString()
        GlobalScope.launch {
            SongRepository.updateSong(song.copy(
                title = title,
                artistName = artistName
            ))
        }
        dismiss()
    }

    companion object {
        const val TAG = "SongDetailsFragment"
    }
}