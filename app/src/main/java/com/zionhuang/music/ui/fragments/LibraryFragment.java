package com.zionhuang.music.ui.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.zionhuang.music.R;
import com.zionhuang.music.viewmodels.LibraryViewModel;

public class LibraryFragment extends BaseFragment {
    private static final String TAG = "LibraryFragment";
    private LibraryViewModel libraryViewModel;

    @Override
    protected int layoutId() {
        return R.layout.fragment_library;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        libraryViewModel = new ViewModelProvider(this).get(LibraryViewModel.class);
        final TextView textView = findViewById(R.id.text_home);
        libraryViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
    }
}