package com.zionhuang.music.compose.screens

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import com.zionhuang.music.R
import com.zionhuang.music.compose.component.AppBarConfig
import com.zionhuang.music.constants.LOCAL
import com.zionhuang.music.constants.ONLINE

@Immutable
sealed class Screen(
    @StringRes val titleId: Int,
    @DrawableRes val iconId: Int,
    val route: String,
) {
    object Home : Screen(R.string.title_home, R.drawable.ic_home, "home")
    object Songs : Screen(R.string.title_songs, R.drawable.ic_music_note, "songs")
    object Artists : Screen(R.string.title_artists, R.drawable.ic_artist, "artists")
    object Albums : Screen(R.string.title_albums, R.drawable.ic_album, "albums")
    object Playlists : Screen(R.string.title_playlists, R.drawable.ic_queue_music, "playlists")
}

fun defaultAppBarConfig(navController: NavController) = AppBarConfig(
    title = {
        Text(
            text = stringResource(R.string.menu_search),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .alpha(0.6f)
                .weight(1f)
        )
    },
    navigationIcon = R.drawable.ic_search,
    onNavigationButtonClick = {
        navController.navigate("search")
    },
    canSearch = true
)

fun searchAppBarConfig(
    navController: NavController,
    searchSource: MutableState<String>,
    onTextFieldValueChange: (TextFieldValue) -> Unit,
) = AppBarConfig(
    onNavigationButtonClick = {
        navController.navigateUp()
        onTextFieldValueChange(TextFieldValue(""))
    },
    canSearch = true,
    searchExpanded = true,
    actions = {
        IconButton(
            onClick = {
                searchSource.value = if (searchSource.value == ONLINE) LOCAL else ONLINE
            }
        ) {
            Icon(
                painter = painterResource(if (searchSource.value == ONLINE) R.drawable.ic_language else R.drawable.ic_library_music),
                contentDescription = null
            )
        }
    }
)

fun onlineSearchResultAppBarConfig(
    navController: NavController,
    query: String,
) = AppBarConfig(
    title = {
        Text(
            text = query,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    },
    navigationIcon = R.drawable.ic_arrow_back,
    onNavigationButtonClick = {
        navController.navigateUp()
        if (navController.currentDestination?.route == "search") {
            navController.navigateUp()
        }
    },
    canSearch = true,
    searchExpanded = false
)

fun albumAppBarConfig(navController: NavController) = AppBarConfig(
    onNavigationButtonClick = {
        navController.navigateUp()
    },
    canSearch = false
)

fun artistAppBarConfig(navController: NavController) = AppBarConfig(
    onNavigationButtonClick = {
        navController.navigateUp()
    },
    canSearch = false
)