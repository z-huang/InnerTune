package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import com.zionhuang.music.db.SongRepository
import com.zionhuang.music.youtube.YouTubeExtractor
import com.zionhuang.music.youtube.models.YouTubeChannel

class ChannelViewModel(application: Application, val channelId: String) : AndroidViewModel(application) {
    private val songRepository: SongRepository = SongRepository(application)
    val channel = songRepository.getChannelById(channelId).asLiveData()
    val channelMeta = liveData {
        val channel = YouTubeExtractor.getInstance(application).getChannel(channelId)
        if (channel is YouTubeChannel.Success) {
            emit(channel)
        }
    }
    val totalDuration: LiveData<Long?> = songRepository.channelSongsDuration(channelId).asLiveData()

    companion object {
        private const val TAG = "ChannelViewModel"
    }
}