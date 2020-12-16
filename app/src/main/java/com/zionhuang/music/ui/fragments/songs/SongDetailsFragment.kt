package com.zionhuang.music.ui.fragments.songs

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.zionhuang.music.R
import com.zionhuang.music.databinding.LayoutSongDetailsBinding
import com.zionhuang.music.db.entities.ArtistEntity
import com.zionhuang.music.ui.fragments.base.MainFragment
import com.zionhuang.music.utils.ArtistAutoCompleteAdapter
import com.zionhuang.music.viewmodels.SongsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SongDetailsFragment : MainFragment<LayoutSongDetailsBinding>() {
    private val songsViewModel by activityViewModels<SongsViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val songId = SongDetailsFragmentArgs.fromBundle(requireArguments()).songId
        lifecycleScope.launch {
            songsViewModel.getSongAsFlow(songId).collectLatest {
                binding.song = it
            }
        }
        binding.songArtistTextField.apply {
            setAdapter(ArtistAutoCompleteAdapter(requireContext()))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_save, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_save) {
            TODO()
        }
        return true
    }

    companion object {
        private const val TAG = "SongDetailsFragment"
    }
}