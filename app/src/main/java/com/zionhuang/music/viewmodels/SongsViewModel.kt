package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.zionhuang.music.db.SongEntity
import com.zionhuang.music.repository.SongRepository

class SongsViewModel(application: Application) : AndroidViewModel(application) {
    private val songRepository: SongRepository = SongRepository(application)
    val allSongs: LiveData<List<SongEntity>> = songRepository.getAllSongsAsLiveData()
}