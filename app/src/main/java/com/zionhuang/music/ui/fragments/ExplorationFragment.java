package com.zionhuang.music.ui.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.zionhuang.music.R;
import com.zionhuang.music.viewmodels.ExplorationViewModel;

public class ExplorationFragment extends BaseFragment {
    private static final String TAG = "ExplorationFragment";
    private ExplorationViewModel explorationViewModel;

    @Override
    protected int layoutId() {
        return R.layout.fragment_exploration;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        explorationViewModel = new ViewModelProvider(this).get(ExplorationViewModel.class);
        final TextView textView = findViewById(R.id.text_exploration);
        explorationViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
    }
}