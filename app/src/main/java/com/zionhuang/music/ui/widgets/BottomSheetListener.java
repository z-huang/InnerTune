package com.zionhuang.music.ui.widgets;

import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

public interface BottomSheetListener {
    void onStateChanged(@NonNull View bottomSheet,@BottomSheetBehavior.State int newState);
    void onSlide(@NonNull View bottomSheet, float slideOffset);
}
