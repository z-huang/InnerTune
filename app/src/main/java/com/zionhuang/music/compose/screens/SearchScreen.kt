package com.zionhuang.music.compose.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zionhuang.innertube.models.SuggestionTextItem
import com.zionhuang.innertube.models.YTItem
import com.zionhuang.music.R
import com.zionhuang.music.compose.component.YouTubeListItem
import com.zionhuang.music.constants.*
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.repos.YouTubeRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchScreen(
    query: String,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
    onSearch: (String) -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val history = rememberSaveable {
        mutableStateOf(emptyList<String>())
    }
    val suggestions = rememberSaveable {
        mutableStateOf(emptyList<String>())
    }
    val onlineSuggestions = rememberSaveable(suggestions.value, history.value) {
        suggestions.value.filter {
            it !in history.value
        }
    }
    val items = rememberSaveable {
        mutableStateOf(emptyList<YTItem>())
    }

    LaunchedEffect(query) {
        delay(200)
        if (query.isEmpty()) {
            SongRepository(context).getAllSearchHistory().collectLatest { list ->
                history.value = list.map { it.query }
            }
        } else {
            SongRepository(context).getSearchHistory(query).collectLatest { list ->
                history.value = list.map { it.query }.take(3)
            }
        }
    }

    LaunchedEffect(query) {
        delay(200)
        if (query.isEmpty()) {
            suggestions.value = emptyList()
            items.value = emptyList()
        } else {
            val result = YouTubeRepository(context).getSuggestions(query)
            suggestions.value = result.filterIsInstance<SuggestionTextItem>().map { it.query }
            items.value = result.filterIsInstance<YTItem>()
        }
    }

    LazyColumn(
        contentPadding = WindowInsets.systemBars
            .only(WindowInsetsSides.Vertical + WindowInsetsSides.End)
            .add(WindowInsets(
                top = AppBarHeight.dp,
                bottom = NavigationBarHeight.dp + MiniPlayerHeight.dp
            ))
            .asPaddingValues()
    ) {
        items(
            items = history.value,
            key = { it }
        ) { query ->
            SuggestionItem(
                query = query,
                online = false,
                onClick = {
                    onSearch(query)
                },
                onDelete = {
                    coroutineScope.launch {
                        SongRepository(context).deleteSearchHistory(query)
                    }
                },
                onFillTextField = {
                    onTextFieldValueChange(TextFieldValue(
                        text = query,
                        selection = TextRange(query.length)
                    ))
                },
                modifier = Modifier.animateItemPlacement()
            )
        }

        items(
            items = onlineSuggestions,
            key = { it }
        ) { query ->
            SuggestionItem(
                query = query,
                online = true,
                onClick = {
                    onSearch(query)
                },
                onFillTextField = {
                    onTextFieldValueChange(TextFieldValue(
                        text = query,
                        selection = TextRange(query.length)
                    ))
                },
                modifier = Modifier.animateItemPlacement()
            )
        }

        if (items.value.isNotEmpty()) {
            item {
                Divider()
            }
        }

        items(
            items = items.value,
            key = { it.id }
        ) { item ->
            YouTubeListItem(
                item = item,
                modifier = Modifier
                    .clickable {

                    }
                    .animateItemPlacement()
            )
        }
    }
}

@Composable
fun SuggestionItem(
    modifier: Modifier = Modifier,
    query: String,
    online: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit = {},
    onFillTextField: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(SuggestionItemHeight.dp)
            .clickable(
                onClick = onClick
            )
    ) {
        Icon(
            painterResource(if (online) R.drawable.ic_search else R.drawable.ic_history),
            contentDescription = null,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .alpha(0.5f)
        )

        Text(
            text = query,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        if (!online) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier.alpha(0.5f)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = null
                )
            }
        }

        IconButton(
            onClick = onFillTextField,
            modifier = Modifier.alpha(0.5f)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_top_left),
                contentDescription = null
            )
        }
    }
}