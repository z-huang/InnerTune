package com.zionhuang.music.ui.recycler;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerPaddingDecoration extends RecyclerView.ItemDecoration {
    private Rect mPadding = new Rect();

    public RecyclerPaddingDecoration(Rect padding) {
        mPadding.set(padding);
    }

    public RecyclerPaddingDecoration(int left, int top, int right, int bottom) {
        mPadding.set(left, top, right, bottom);
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int left = mPadding.left;
        int right = mPadding.right;
        int top = 0;
        int bottom = 0;

        int adapterPosition = parent.getChildAdapterPosition(view);
        if (adapterPosition == 0) {
            top = mPadding.top;
        }
        if (adapterPosition == state.getItemCount() - 1) {
            bottom = mPadding.bottom;
        }

        outRect.set(left, top, right, bottom);
    }
}
