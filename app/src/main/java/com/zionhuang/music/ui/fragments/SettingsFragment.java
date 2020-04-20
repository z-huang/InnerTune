package com.zionhuang.music.ui.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.zionhuang.music.R;
import com.zionhuang.music.viewmodels.SettingsViewModel;

public class SettingsFragment extends BaseFragment {
    private static final String TAG = "SettingsFragment";
    private SettingsViewModel mViewModel;

    @Override
    protected int layoutId() {
        return R.layout.fragment_settings;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        final TextView textView = findViewById(R.id.text_settings);
        mViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
    }
}
