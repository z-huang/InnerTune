package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import com.zionhuang.music.db.SongRepository
import com.zionhuang.music.extensions.getChannelAvatarFile
import com.zionhuang.music.extensions.getChannelBannerFile
import com.zionhuang.music.youtube.models.YouTubeChannel

class ChannelViewModel(application: Application, val channelId: String) : AndroidViewModel(application) {
    private val songRepository: SongRepository = SongRepository(application)
    val channel = songRepository.getChannelFlowById(channelId).asLiveData()
    val channelMeta = liveData {
        val avatarFile = application.getChannelAvatarFile(channelId)
        val bannerFile = application.getChannelBannerFile(channelId)
        if (!avatarFile.exists() || !bannerFile.exists()) {
            songRepository.downloadChannel(channelId)
        }
        emit(YouTubeChannel.Success(channelId, null, avatarFile.path, bannerFile.path))
    }
    val totalDuration: LiveData<Long?> = songRepository.channelSongsDuration(channelId).asLiveData()

    companion object {
        private const val TAG = "ChannelViewModel"
    }
}