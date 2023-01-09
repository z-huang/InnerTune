package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zionhuang.music.lyrics.LyricsHelper
import com.zionhuang.music.lyrics.LyricsResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LyricsMenuViewModel(val app: Application) : AndroidViewModel(app) {
    private var job: Job? = null
    val results = MutableStateFlow(emptyList<LyricsResult>())
    val isLoading = MutableStateFlow(false)

    fun search(mediaId: String, title: String, artist: String, duration: Int) {
        isLoading.value = true
        results.value = emptyList()
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            LyricsHelper.getAllLyrics(app, mediaId, title, artist, duration) { result ->
                results.update {
                    it + result
                }
            }
            isLoading.value = false
        }
    }

    fun cancelSearch() {
        job?.cancel()
        job = null
    }
}