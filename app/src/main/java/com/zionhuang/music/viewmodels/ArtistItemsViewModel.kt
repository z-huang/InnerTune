package com.zionhuang.music.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.BrowseEndpoint
import com.zionhuang.music.models.ItemsPage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArtistItemsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val browseId = savedStateHandle.get<String>("browseId")!!
    private val params = savedStateHandle.get<String>("params")

    val title = MutableStateFlow("")
    val itemsPage = MutableStateFlow<ItemsPage?>(null)

    init {
        viewModelScope.launch {
            val artistItemsPage = YouTube.artistItems(BrowseEndpoint(
                browseId = browseId,
                params = params
            )).getOrNull() ?: return@launch
            title.value = artistItemsPage.title
            itemsPage.value = ItemsPage(
                items = artistItemsPage.items,
                continuation = artistItemsPage.continuation
            )
        }
    }

    fun loadMore() {
        viewModelScope.launch {
            val oldItemsPage = itemsPage.value ?: return@launch
            val continuation = oldItemsPage.continuation ?: return@launch
            val artistItemsContinuationPage = YouTube.artistItemsContinuation(continuation).getOrNull() ?: return@launch
            itemsPage.update {
                ItemsPage(
                    items = (oldItemsPage.items + artistItemsContinuationPage.items).distinctBy { it.id },
                    continuation = artistItemsContinuationPage.continuation
                )
            }
        }
    }
}
