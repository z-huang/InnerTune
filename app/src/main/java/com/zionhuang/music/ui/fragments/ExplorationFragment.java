package com.zionhuang.music.ui.fragments;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_search_icon, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            NavHostFragment.findNavController(this).navigate(R.id.action_explorationFragment_to_searchSuggestionFragment);
        }
        return true;
    }
}