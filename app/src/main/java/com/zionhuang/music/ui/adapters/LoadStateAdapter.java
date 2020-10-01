package com.zionhuang.music.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.paging.LoadState;

import com.zionhuang.music.databinding.ItemLoadStateBinding;
import com.zionhuang.music.ui.viewholders.LoadStateViewHolder;

import org.jetbrains.annotations.NotNull;

public class LoadStateAdapter extends androidx.paging.LoadStateAdapter<LoadStateViewHolder> {
    Runnable mRetryFn;

    public LoadStateAdapter(Runnable retryFn) {
        mRetryFn = retryFn;
    }

    @Override
    public void onBindViewHolder(@NotNull LoadStateViewHolder holder, @NotNull LoadState loadState) {
        holder.bind(loadState);
    }

    @NotNull
    @Override
    public LoadStateViewHolder onCreateViewHolder(@NotNull ViewGroup parent, @NotNull LoadState loadState) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemLoadStateBinding loadStateBinding = ItemLoadStateBinding.inflate(inflater, parent, false);
        return new LoadStateViewHolder(loadStateBinding, mRetryFn);
    }
}
