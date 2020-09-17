package com.zionhuang.music.ui.adapters.selection;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Selection<Key> {

    private KeyStorage<Key> mKeyStorage;

    private Set<Observer<Key>> mObservers = new HashSet<>();
    private MutableLiveData<Selection<Key>> mLiveSelection = new MutableLiveData<>(this);

    public Selection(KeyStorage<Key> keyStorage) {
        mKeyStorage = keyStorage;
    }

    public void setSelected(Key key, boolean selected) {
        boolean currentlySelected = mKeyStorage.isStored(key);
        if ((currentlySelected && selected) || (!currentlySelected && !selected))
            return;

        if (selected)
            mKeyStorage.store(key);
        else
            mKeyStorage.remove(key);

        for (Observer<Key> observer : mObservers) {
            observer.onKeySelectionChanged(this, key, selected);
        }
        mLiveSelection.setValue(this);
    }


    /**
     * Select/deselect all keys in given collection. Useful when you want to change selection for a lot of keys, but don't want to trigger observer for every key.
     *
     * @param keys     keys to change selection for
     * @param selected new selection status of given keys
     */
    public void batchSetSelected(Collection<Key> keys, boolean selected) {
        if (selected)
            mKeyStorage.storeAll(keys);
        else
            mKeyStorage.removeAll(keys);

        for (Observer<Key> observer : mObservers) {
            observer.onMultipleKeysSelectionChanged(this, keys, selected);
        }
        mLiveSelection.setValue(this);
    }

    public boolean switchSelection(Key key) {
        boolean isSelected = isSelected(key);
        setSelected(key, !isSelected);
        return !isSelected;
    }

    public boolean isSelected(Key key) {
        return mKeyStorage.isStored(key);
    }

    public Collection<Key> getSelectedKeys() {
        return mKeyStorage.getAllStoredKeys();
    }

    public int size() {
        return mKeyStorage.getStoredKeysCount();
    }

    public boolean hasSelection() {
        return size() > 0;
    }

    public void clear() {
        mKeyStorage.clear();

        for (Observer<Key> observer : mObservers) {
            observer.onCleared(this);
        }
        mLiveSelection.setValue(this);
    }

    public void observe(LifecycleOwner lifecycleOwner, Observer<Key> observer) {
        if (lifecycleOwner.getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED)
            return;

        addObserver(observer);
        lifecycleOwner.getLifecycle().addObserver(new DefaultLifecycleObserver() {
            @Override
            public void onDestroy(@NonNull LifecycleOwner owner) {
                removeObserver(observer);
            }
        });
    }

    public void addObserver(Observer<Key> observer) {
        mObservers.add(observer);
    }

    public void removeObserver(Observer<Key> observer) {
        mObservers.remove(observer);
    }

    public LiveData<Selection<Key>> asLiveData() {
        return mLiveSelection;
    }


    public interface Observer<Key> {

        void onKeySelectionChanged(Selection<Key> selection, Key key, boolean selected);

        void onCleared(Selection<Key> selection);

        /**
         * Called when selection has been changed via batch methods such as {@link #batchSetSelected(Collection, boolean)}
         * Note, that when selection is cleared, this method won't be called, {@link #onCleared(Selection)} will be called instead.
         *
         * @param selection selection that has been changed
         * @param keys      keys that has been changed. Note that these keys may not have actually been in this selection before
         * @param selected  whether these keys has been selected or deselected
         */
        void onMultipleKeysSelectionChanged(Selection<Key> selection, Collection<Key> keys, boolean selected);

    }


    public interface KeyStorage<K> {
        void store(K key);

        void remove(K key);

        void storeAll(Collection<K> keys);

        void removeAll(Collection<K> keys);

        boolean isStored(K key);

        Collection<K> getAllStoredKeys();

        int getStoredKeysCount();

        void clear();
    }

}
