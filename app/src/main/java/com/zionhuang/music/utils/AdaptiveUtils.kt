package com.zionhuang.music.utils

import android.content.Context
import android.content.res.Configuration.ORIENTATION_UNDEFINED
import android.util.DisplayMetrics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object AdaptiveUtils {

    enum class ScreenSize {
        SMALL, MEDIUM, LARGE, XLARGE
    }

    enum class ContentState {
        SINGLE_PANE, DUAL_PANE
    }

    private const val SMALL_SCREEN_SIZE_UPPER_THRESHOLD = 700
    private const val MEDIUM_SCREEN_SIZE_UPPER_THRESHOLD = 840
    private const val LARGE_SCREEN_SIZE_UPPER_THRESHOLD = 1024

    private val _orientation = MutableStateFlow(ORIENTATION_UNDEFINED)
    val orientation: StateFlow<Int> = _orientation
    private val _screenSizeState = MutableStateFlow(ScreenSize.SMALL)
    val screenSizeState: StateFlow<ScreenSize> = _screenSizeState

    private val _contentState = MutableStateFlow(ContentState.SINGLE_PANE)
    val contentState: StateFlow<ContentState> = _contentState

    fun updateContentState(singlePane: Boolean) {
        val newState = if (singlePane) ContentState.SINGLE_PANE else ContentState.DUAL_PANE
        if (_contentState.value == newState) return
        _contentState.value = newState
    }

    fun updateScreenSize(context: Context) {
        val displayMetrics: DisplayMetrics = context.resources.displayMetrics
        val screenWidth = (displayMetrics.widthPixels / displayMetrics.density).toInt()
        val newState = when {
            screenWidth < SMALL_SCREEN_SIZE_UPPER_THRESHOLD -> ScreenSize.SMALL
            screenWidth in SMALL_SCREEN_SIZE_UPPER_THRESHOLD until MEDIUM_SCREEN_SIZE_UPPER_THRESHOLD -> ScreenSize.MEDIUM
            screenWidth in MEDIUM_SCREEN_SIZE_UPPER_THRESHOLD until LARGE_SCREEN_SIZE_UPPER_THRESHOLD -> ScreenSize.LARGE
            else -> ScreenSize.XLARGE
        }
        if (_screenSizeState.value == newState) return
        _screenSizeState.value = newState
    }

    fun updateOrientation(context: Context) {
        _orientation.value = context.resources.configuration.orientation
    }
}