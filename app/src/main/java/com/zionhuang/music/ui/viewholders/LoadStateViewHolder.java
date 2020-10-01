package com.zionhuang.music.ui.viewholders;

import android.view.View;

import androidx.paging.LoadState;
import androidx.recyclerview.widget.RecyclerView;

import com.zionhuang.music.databinding.ItemLoadStateBinding;

public class LoadStateViewHolder extends RecyclerView.ViewHolder {
    private ItemLoadStateBinding binding;

    public LoadStateViewHolder(ItemLoadStateBinding binding, Runnable mRetryFn) {
        super(binding.getRoot());
        this.binding = binding;
        binding.btnRetry.setOnClickListener(v -> mRetryFn.run());
    }

    public void bind(LoadState loadState) {
        if (loadState instanceof LoadState.Error) {
            binding.tvErrorMsg.setText(((LoadState.Error) loadState).getError().getLocalizedMessage());
        }
        binding.progressBar.setVisibility(loadState instanceof LoadState.Loading ? View.VISIBLE : View.GONE);
        binding.btnRetry.setVisibility(!(loadState instanceof LoadState.Loading) ? View.VISIBLE : View.GONE);
        binding.tvErrorMsg.setVisibility(!(loadState instanceof LoadState.Loading) ? View.VISIBLE : View.GONE);
    }
}
