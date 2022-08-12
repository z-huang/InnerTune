package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zionhuang.innertube.models.YTBaseItem
import com.zionhuang.music.repos.YouTubeRepository
import kotlinx.coroutines.launch

class SuggestionViewModel(application: Application) : AndroidViewModel(application) {
    val suggestions = MutableLiveData<List<YTBaseItem>>(emptyList())

    fun fetchSuggestions(query: String?) {
        if (query.isNullOrEmpty()) {
            suggestions.postValue(emptyList())
            return
        }
        viewModelScope.launch {
            try {
                suggestions.postValue(YouTubeRepository.getSuggestions(query))
            } catch (e: Exception) {
            }
        }
    }
}