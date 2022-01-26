package com.zionhuang.music.viewmodels

import android.app.Application
import android.content.Context
import androidx.core.os.bundleOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zionhuang.music.constants.MediaConstants.EXTRA_ARTIST
import com.zionhuang.music.db.SongRepository
import com.zionhuang.music.db.entities.ArtistEntity
import com.zionhuang.music.extensions.getActivity
import com.zionhuang.music.ui.activities.MainActivity
import com.zionhuang.music.ui.fragments.dialogs.EditArtistDialog
import com.zionhuang.music.ui.listeners.ArtistPopupMenuListener
import kotlinx.coroutines.launch

class ArtistsViewModel(application: Application) : AndroidViewModel(application) {
    private val songRepository = SongRepository()

    val popupMenuListener = object : ArtistPopupMenuListener {
        override fun editArtist(artist: ArtistEntity, context: Context) {
            (context.getActivity() as? MainActivity)?.let { activity ->
                EditArtistDialog().apply {
                    arguments = bundleOf(EXTRA_ARTIST to artist)
                }.show(activity.supportFragmentManager, EditArtistDialog::class.simpleName)
            }
        }

        override fun deleteArtist(artist: ArtistEntity) {
            viewModelScope.launch {
                songRepository.deleteArtist(artist)
            }
        }
    }
}