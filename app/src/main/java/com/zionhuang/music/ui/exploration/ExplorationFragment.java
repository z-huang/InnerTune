package com.zionhuang.music.ui.exploration;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.zionhuang.music.R;

public class ExplorationFragment extends Fragment {

    private ExplorationViewModel explorationViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d("Exploration Fragment", "on create view");
        ActionBar toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        explorationViewModel = new ViewModelProvider(this).get(ExplorationViewModel.class);
        View root = inflater.inflate(R.layout.fragment_exploration, container, false);
        final TextView textView = root.findViewById(R.id.text_exploration);
        explorationViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}