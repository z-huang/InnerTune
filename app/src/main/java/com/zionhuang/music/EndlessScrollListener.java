package com.zionhuang.music;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public abstract class EndlessScrollListener extends RecyclerView.OnScrollListener {
    private int previousTotalItemCount = 0;
    private boolean loading = true;
    private LinearLayoutManager layoutManager;

    public EndlessScrollListener(LinearLayoutManager linearLayoutManager) {
        layoutManager = linearLayoutManager;
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        if (dy <= 0) {
            return;
        }
        int totalItemCount = layoutManager.getItemCount();
        int lastVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition();
        if (loading) {
            if (totalItemCount > previousTotalItemCount + 1) {
                loading = false;
                previousTotalItemCount = totalItemCount;
            }
        } else if (lastVisibleItemPosition == totalItemCount-1 && hasMore()) {
            loading = true;
            loadMore();
        }
    }

    public abstract boolean hasMore();

    public abstract void loadMore();
}
