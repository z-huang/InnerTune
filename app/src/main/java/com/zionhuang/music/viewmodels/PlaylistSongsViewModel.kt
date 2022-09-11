package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.zionhuang.music.repos.SongRepository

class PlaylistSongsViewModel(application: Application) : AndroidViewModel(application) {
    private val songRepository = SongRepository
}