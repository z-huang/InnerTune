package com.zionhuang.music.ui.fragments.songs

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.zionhuang.music.databinding.LayoutSongDetailsBinding
import com.zionhuang.music.repository.SongRepository
import com.zionhuang.music.ui.fragments.base.BindingFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SongDetailsFragment : BindingFragment<LayoutSongDetailsBinding>() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val songId = SongDetailsFragmentArgs.fromBundle(requireArguments()).songId
        val songRepository = SongRepository(requireContext())
        lifecycleScope.launch {
            songRepository.getSongAsFlow(songId).collectLatest {
                binding.song = it
            }
        }
    }
}