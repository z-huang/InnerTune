package com.zionhuang.music.utils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public abstract class EndlessScrollListener extends RecyclerView.OnScrollListener {
    private int previousTotalItemCount = 0;
    private boolean loading = true;
    private LinearLayoutManager mLayoutManager;

    protected EndlessScrollListener(LinearLayoutManager linearLayoutManager) {
        mLayoutManager = linearLayoutManager;
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        if (dy <= 0) {
            return;
        }
        int totalItemCount = mLayoutManager.getItemCount();
        int lastVisibleItemPosition = mLayoutManager.findLastCompletelyVisibleItemPosition();
        if (loading) {
            if (totalItemCount > previousTotalItemCount + 1) {
                loading = false;
                previousTotalItemCount = totalItemCount;
            }
        } else if (lastVisibleItemPosition == totalItemCount - 1 && hasMore()) {
            loading = true;
            loadMore();
        }
    }

    public abstract boolean hasMore();

    public abstract void loadMore();
}
