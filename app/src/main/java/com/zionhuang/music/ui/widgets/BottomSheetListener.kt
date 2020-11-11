package com.zionhuang.music.ui.widgets

import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior

interface BottomSheetListener {
    fun onStateChanged(bottomSheet: View, @BottomSheetBehavior.State newState: Int)
    fun onSlide(bottomSheet: View, slideOffset: Float)
}