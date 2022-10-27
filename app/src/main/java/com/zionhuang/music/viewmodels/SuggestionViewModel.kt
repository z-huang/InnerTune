package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zionhuang.innertube.models.SuggestionTextItem
import com.zionhuang.innertube.models.SuggestionTextItem.SuggestionSource.LOCAL
import com.zionhuang.innertube.models.YTBaseItem
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.repos.YouTubeRepository
import kotlinx.coroutines.launch

class SuggestionViewModel(application: Application) : AndroidViewModel(application) {
    private val songRepository = SongRepository(application)
    private val youTubeRepository = YouTubeRepository(application)
    val suggestions = MutableLiveData<List<YTBaseItem>>(emptyList())

    fun fetchSuggestions(query: String?) = viewModelScope.launch {
        if (query.isNullOrEmpty()) {
            suggestions.postValue(songRepository.getAllSearchHistory().map { SuggestionTextItem(it.query, LOCAL) })
        } else {
            try {
                val history = songRepository.getSearchHistory(query).map { SuggestionTextItem(it.query, LOCAL) }
                suggestions.postValue(history + youTubeRepository.getSuggestions(query).filter { item ->
                    item !is SuggestionTextItem || history.find { it.query == item.query } == null
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}