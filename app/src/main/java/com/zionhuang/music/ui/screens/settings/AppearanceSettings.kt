package com.zionhuang.music.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.zionhuang.music.LocalPlayerAwareWindowInsets
import com.zionhuang.music.R
import com.zionhuang.music.constants.DARK_THEME
import com.zionhuang.music.constants.DEFAULT_OPEN_TAB
import com.zionhuang.music.constants.LYRICS_TEXT_POSITION
import com.zionhuang.music.extensions.mutablePreferenceState
import com.zionhuang.music.ui.component.EnumListPreference

@Composable
fun AppearanceSettings() {
    val (darkMode, onDarkModeChange) = mutablePreferenceState(key = DARK_THEME, defaultValue = DarkMode.AUTO)
    val (defaultOpenTab, onDefaultOpenTabChange) = mutablePreferenceState(key = DEFAULT_OPEN_TAB, defaultValue = NavigationTab.HOME)
    val (lyricsPosition, onLyricsPositionChange) = mutablePreferenceState(key = LYRICS_TEXT_POSITION, defaultValue = LyricsPosition.CENTER)

    Column(
        Modifier
            .padding(LocalPlayerAwareWindowInsets.current.asPaddingValues())
            .verticalScroll(rememberScrollState())
    ) {
        EnumListPreference(
            title = stringResource(R.string.pref_dark_theme_title),
            icon = R.drawable.ic_dark_mode,
            selectedValue = darkMode,
            onValueSelected = onDarkModeChange,
            valueText = {
                when (it) {
                    DarkMode.ON -> stringResource(R.string.dark_theme_on)
                    DarkMode.OFF -> stringResource(R.string.dark_theme_off)
                    DarkMode.AUTO -> stringResource(R.string.dark_theme_follow_system)
                }
            }
        )
        EnumListPreference(
            title = stringResource(R.string.pref_default_open_tab_title),
            icon = R.drawable.ic_tab,
            selectedValue = defaultOpenTab,
            onValueSelected = onDefaultOpenTabChange,
            valueText = {
                when (it) {
                    NavigationTab.HOME -> stringResource(R.string.title_home)
                    NavigationTab.SONG -> stringResource(R.string.title_songs)
                    NavigationTab.ARTIST -> stringResource(R.string.title_artists)
                    NavigationTab.ALBUM -> stringResource(R.string.title_albums)
                    NavigationTab.PLAYLIST -> stringResource(R.string.title_playlists)
                }
            }
        )
        EnumListPreference(
            title = stringResource(R.string.pref_lyrics_text_position_title),
            icon = R.drawable.ic_lyrics,
            selectedValue = lyricsPosition,
            onValueSelected = onLyricsPositionChange,
            valueText = {
                when (it) {
                    LyricsPosition.LEFT -> stringResource(R.string.align_left)
                    LyricsPosition.CENTER -> stringResource(R.string.align_center)
                    LyricsPosition.RIGHT -> stringResource(R.string.align_right)
                }
            }
        )
    }
}

enum class DarkMode {
    ON, OFF, AUTO
}

enum class NavigationTab {
    HOME, SONG, ARTIST, ALBUM, PLAYLIST
}

enum class LyricsPosition {
    LEFT, CENTER, RIGHT
}