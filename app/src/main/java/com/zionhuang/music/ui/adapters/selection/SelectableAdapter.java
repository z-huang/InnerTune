package com.zionhuang.music.ui.adapters.selection;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public abstract class SelectableAdapter<Key, ViewHolder extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<ViewHolder> {
    private static final String TAG = "SelectableAdapter";

    private final Selection<Key> mSelection;
    private final HashMap<Key, Integer> mKeyToPosition = new HashMap<>();
    @SuppressLint("UseSparseArrays")
    private final HashMap<Integer, Key> mPositionToKey = new HashMap<>();

    private final Selection.Observer<Key> mSelectionObserver = new Selection.Observer<Key>() {
        @Override
        public void onKeySelectionChanged(Selection<Key> selection, Key key, boolean selected) {
            Integer position = mKeyToPosition.get(key);
            if (position != null)
                notifyItemChanged(position);
        }

        @Override
        public void onCleared(Selection<Key> selection) {
            clearKeysMapping();
            notifyDataSetChanged();
        }

        @Override
        public void onMultipleKeysSelectionChanged(Selection<Key> selection, Collection<Key> keys, boolean selected) {
            Set<Integer> positionsToUpdate = new HashSet<>();
            for (Key key : keys) {
                Integer position = mKeyToPosition.get(key);
                if (position != null)
                    positionsToUpdate.add(position);
            }

            //Apparently calling notifyItemChanged for every item is a bit laggy when a lot of bound positions require updating, notifyDataSetChanged works better in that case
            if (positionsToUpdate.size() > mKeyToPosition.size() / 2) {
                Log.d(TAG, "onMultipleKeysSelectionChanged: Update of more than a half of bound positions required, using notifyDataSetChanged");
                notifyDataSetChanged();
            } else {
                for (Integer position : positionsToUpdate)
                    notifyItemChanged(position);
            }
        }
    };

    private final RecyclerView.AdapterDataObserver mAdapterObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            clearKeysMapping();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            for (int i = positionStart; i < positionStart + itemCount; i++)
                mKeyToPosition.remove(mPositionToKey.remove(i));
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            //TODO optimize this
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            //TODO optimize this
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            //TODO optimize this
            notifyDataSetChanged();
        }
    };

    private RecyclerView mRecycler;

    public SelectableAdapter(Selection<Key> selection, LifecycleOwner lifecycleOwner) {
        mSelection = selection;

        if (lifecycleOwner.getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED)
            return;

        lifecycleOwner.getLifecycle().addObserver(new DefaultLifecycleObserver() {
            @Override
            public void onDestroy(@NonNull LifecycleOwner owner) {
                unregisterObservers();
            }
        });
    }

    public final Selection<Key> getSelection() {
        return mSelection;
    }

    protected final boolean isSelected(Key key) {
        return mSelection.isSelected(key);
    }

    protected final void setSelected(Key key, boolean selected) {
        mSelection.setSelected(key, selected);
    }

    protected final boolean switchSelection(Key key) {
        return mSelection.switchSelection(key);
    }

    protected abstract Key getKeyForPosition(int position);

    @CallSuper
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Key key = getKeyForPosition(position);
        mKeyToPosition.put(key, position);
        mPositionToKey.put(position, key);
    }

    @CallSuper
    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        int adapterPosition = holder.getAdapterPosition();
        if (adapterPosition == RecyclerView.NO_POSITION)
            return;

        //onViewRecycled calls seem to be batched after onBindViewHolder calls, that will lead to clearing an actually required key without this check
        if (mRecycler.findViewHolderForAdapterPosition(adapterPosition) == null) {
            Key key = mPositionToKey.remove(adapterPosition);
            if (key != null)
                mKeyToPosition.remove(key);
        }
    }

    @CallSuper
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        mRecycler = recyclerView;
        registerObservers();
    }

    @CallSuper
    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        unregisterObservers();
        mRecycler = null;
    }

    private void clearKeysMapping() {
        mPositionToKey.clear();
        mKeyToPosition.clear();
    }

    private void registerObservers() {
        mSelection.addObserver(mSelectionObserver);
        registerAdapterDataObserver(mAdapterObserver);
    }

    private void unregisterObservers() {
        mSelection.removeObserver(mSelectionObserver);
        unregisterAdapterDataObserver(mAdapterObserver);
    }
}
