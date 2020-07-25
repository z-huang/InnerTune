package com.zionhuang.music.utils;

import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class FragmentNavigator {
    private FragmentManager mFragmentManager;
    @IdRes
    private int mContainerId;
    private FragmentFactory mFragmentFactory;
    private Fragment mCurrentFragment;
    private boolean mWasRestoreStateCalled = false;

    public FragmentNavigator(FragmentManager fragmentManager, @IdRes int containerId, FragmentFactory fragmentFactory) {
        mFragmentManager = fragmentManager;
        mContainerId = containerId;
        mFragmentFactory = fragmentFactory;
    }

    public FragmentNavigator(@Nullable Bundle savedInstanceState, FragmentManager fragmentManager, @IdRes int containerId, FragmentFactory fragmentFactory) {
        this(fragmentManager, containerId, fragmentFactory);
        restoreState(savedInstanceState);
    }

    public void switchTo(String tag, @Nullable Bundle bundle) {
        ensureStateWasRestored();
        if (mCurrentFragment != null && tag.equals(mCurrentFragment.getTag())) {
            return;
        }
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        if (mCurrentFragment != null) {
            transaction.hide(mCurrentFragment);
        }
        Fragment newFragment = mFragmentManager.findFragmentByTag(tag);
        if (newFragment != null) {
            transaction.show(newFragment);
        } else {
            newFragment = mFragmentFactory.createFragment(tag);
            transaction.add(mContainerId, newFragment, tag);
        }
        newFragment.setArguments(bundle);
        mCurrentFragment = newFragment;
        transaction.commitNow();
    }

    public void switchTo(String tag) {
        switchTo(tag, null);
    }

    public Fragment getCurrentFragment() {
        return mCurrentFragment;
    }

    public Fragment findFragmentByTag(String tag) {
        ensureStateWasRestored();
        return mFragmentManager.findFragmentByTag(tag);
    }

    public void writeStateToBundle(@NonNull Bundle bundle) {
        bundle.putString("fragmentNavigatorCurrentFragment", mCurrentFragment != null ? mCurrentFragment.getTag() : null);
    }

    private void restoreState(@Nullable Bundle bundle) {
        mWasRestoreStateCalled = true;
        if (bundle == null) {
            return;
        }
        String currentFragmentTag = bundle.getString("fragmentNavigatorCurrentFragment", null);
        if (currentFragmentTag != null) {
            mCurrentFragment = mFragmentManager.findFragmentByTag(currentFragmentTag);
        }
    }

    private void ensureStateWasRestored() {
        if (!mWasRestoreStateCalled) {
            throw new IllegalStateException("Please call restoreState before using this FragmentNavigator");
        }
    }

    public void replaceFragment(@IdRes int id, Fragment fragment) {
        replaceFragment(id, fragment, null, false);
    }

    public void replaceFragment(@IdRes int id, Fragment fragment, String tag, boolean addToBackStack) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(id, fragment, tag);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    public interface FragmentFactory {
        Fragment createFragment(String tag);
    }
}
