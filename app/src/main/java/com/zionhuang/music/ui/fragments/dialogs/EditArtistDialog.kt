package com.zionhuang.music.ui.fragments.dialogs

import android.app.Dialog
import android.content.DialogInterface.BUTTON_POSITIVE
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.widget.doOnTextChanged
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zionhuang.music.R
import com.zionhuang.music.constants.MediaConstants.EXTRA_ARTIST
import com.zionhuang.music.databinding.DialogSingleTextInputBinding
import com.zionhuang.music.db.entities.ArtistEntity
import com.zionhuang.music.repos.SongRepository
import kotlinx.coroutines.*

class EditArtistDialog : AppCompatDialogFragment() {
    private lateinit var binding: DialogSingleTextInputBinding
    private lateinit var artist: ArtistEntity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        artist = arguments?.getParcelable(EXTRA_ARTIST)!!
    }

    private fun setupUI() {
        binding.textInput.apply {
            setHint(R.string.text_view_hint_artist_name)
            editText?.setText(artist.name)
            editText?.doOnTextChanged { text, _, _, _ ->
                binding.textInput.error = if (text.isNullOrEmpty()) getString(R.string.error_artist_name_empty) else null
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogSingleTextInputBinding.inflate(requireActivity().layoutInflater)
        setupUI()

        return MaterialAlertDialogBuilder(requireContext(), R.style.Dialog)
            .setTitle(R.string.dialog_title_edit_artist)
            .setView(binding.root)
            .setPositiveButton(R.string.dialog_button_save, null)
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
        GlobalScope.launch {
            val songRepository = SongRepository(requireContext())
            val existedArtist = songRepository.getArtistByName(name)
            if (existedArtist != null && existedArtist.id != artist.id) {
                // name exists
                withContext(Dispatchers.Main) {
                    MaterialAlertDialogBuilder(requireContext(), R.style.Dialog)
                        .setTitle(getString(R.string.dialog_title_duplicate_artist))
                        .setMessage(getString(R.string.dialog_msg_duplicate_artist, existedArtist.name))
                        .setPositiveButton(resources.getString(android.R.string.ok), null)
                        .show()
                }
            } else {
                songRepository.updateArtist(artist.copy(name = name))
                dismiss()
            }
        }
    }
}