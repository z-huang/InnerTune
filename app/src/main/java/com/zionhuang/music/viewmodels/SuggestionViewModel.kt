package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zionhuang.music.youtube.YouTubeRepository
import com.zionhuang.music.youtube.YouTubeRepository.Companion.getInstance
import com.zionhuang.music.youtube.newpipe.ExtractorHelper
import kotlinx.coroutines.launch

class SuggestionViewModel(application: Application) : AndroidViewModel(application) {
    private val youtubeRepo: YouTubeRepository = getInstance(application)
    val onFillQuery = MutableLiveData<String?>()
    val query = MutableLiveData<String?>(null)
    val suggestions = MutableLiveData<List<String>>(emptyList())

    fun fillQuery(q: String) {
        onFillQuery.postValue(q)
    }

    fun setQuery(q: String?) {
        query.postValue(q)
    }

    fun fetchSuggestions(query: String?) {
        if (query.isNullOrEmpty()) {
            suggestions.postValue(emptyList())
            return
        }
        viewModelScope.launch {
            suggestions.postValue(ExtractorHelper.suggestionsFor(query))
        }
    }
}