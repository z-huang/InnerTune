package com.zionhuang.music.ui.widgets;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewClickManager implements RecyclerView.OnChildAttachStateChangeListener {
    private RecyclerView mRecyclerView;
    private Listener mClickListener;
    private Listener mLongClickListener;

    public RecyclerViewClickManager(RecyclerView recyclerView, Listener clickListener, Listener longClickListener) {
        mRecyclerView = recyclerView;
        mClickListener = clickListener;
        mLongClickListener = longClickListener;
        mRecyclerView.addOnChildAttachStateChangeListener(this);
    }

    public static void setup(RecyclerView recyclerView, Listener clickListener, Listener longClickListener) {
        new RecyclerViewClickManager(recyclerView, clickListener, longClickListener);
    }

    @Override
    public void onChildViewAttachedToWindow(@NonNull View view) {
        view.setOnClickListener(v -> handleEvent(view, mClickListener));
        view.setOnLongClickListener(v -> {
            handleEvent(view, mLongClickListener);
            return true;
        });
    }

    @Override
    public void onChildViewDetachedFromWindow(@NonNull View view) {
        view.setOnClickListener(null);
        view.setOnLongClickListener(null);
    }

    private void handleEvent(View v, Listener listener) {
        if (v != null) {
            int position = mRecyclerView.getChildLayoutPosition(v);
            if (position >= 0) {
                if (listener != null) {
                    listener.apply(position, v);
                }
            }
        }
    }

    public interface Listener {
        void apply(int position, View v);
    }
}
