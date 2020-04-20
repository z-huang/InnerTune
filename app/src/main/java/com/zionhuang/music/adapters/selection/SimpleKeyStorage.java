package com.zionhuang.music.adapters.selection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * Designed to be stored within a ViewModel
 */
public class SimpleKeyStorage<Key> implements Selection.KeyStorage<Key> {

    private HashSet<Key> mKeys = new HashSet<>();

    @Override
    public void store(Key key) {
        mKeys.add(key);
    }

    @Override
    public void remove(Key key) {
        mKeys.remove(key);
    }

    @Override
    public void storeAll(Collection<Key> keys) {
        mKeys.addAll(keys);
    }

    @Override
    public void removeAll(Collection<Key> keys) {
        mKeys.removeAll(keys);
    }

    @Override
    public boolean isStored(Key key) {
        return mKeys.contains(key);
    }

    @Override
    public Collection<Key> getAllStoredKeys() {
        return new ArrayList<>(mKeys);
    }

    @Override
    public int getStoredKeysCount() {
        return mKeys.size();
    }

    @Override
    public void clear() {
        mKeys.clear();
    }
}
