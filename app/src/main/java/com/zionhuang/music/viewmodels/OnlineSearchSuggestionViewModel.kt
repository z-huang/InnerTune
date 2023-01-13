package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.YTItem
import com.zionhuang.music.repos.SongRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class OnlineSearchSuggestionViewModel(app: Application) : AndroidViewModel(app) {
    private val songRepository = SongRepository(app)
    val query = MutableStateFlow("")
    private val _viewState = MutableStateFlow(SearchSuggestionViewState())
    val viewState = _viewState.asStateFlow()

    init {
        viewModelScope.launch {
            query.flatMapLatest { query ->
                if (query.isEmpty()) {
                    songRepository.getAllSearchHistory().map { history ->
                        SearchSuggestionViewState(
                            history = history.map { it.query }
                        )
                    }
                } else {
                    val result = YouTube.getSearchSuggestions(query).getOrNull()
                    songRepository.getSearchHistory(query).map { searchHistory ->
                        val history = searchHistory.map { it.query }.take(3)
                        SearchSuggestionViewState(
                            history = history,
                            suggestions = result?.queries?.filter { it !in history }.orEmpty(),
                            items = result?.recommendedItems.orEmpty()
                        )
                    }
                }
            }.collect {
                _viewState.value = it
            }
        }
    }
}

data class SearchSuggestionViewState(
    val history: List<String> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val items: List<YTItem> = emptyList(),
)
