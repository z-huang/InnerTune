package com.zionhuang.music.ui.fragments;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.zionhuang.music.R;
import com.zionhuang.music.viewmodels.LibraryViewModel;

import java.util.ArrayList;

public class LibraryFragment extends BaseFragment {
    private static final String TAG = "LibraryFragment";
    private LibraryViewModel libraryViewModel;
    private ViewPager mViewPager;

    @Override
    protected int layoutId() {
        return R.layout.fragment_library;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewPager = findViewById(R.id.viewpager);
        setupViewPager(mViewPager);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_search_icon, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return true;
    }

    private void setupViewPager(ViewPager viewPager) {
        LibraryAdapter adapter = new LibraryAdapter(getChildFragmentManager());
        adapter.addFragment(new SongsFragment(), "All Songs");

        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setCurrentItem(0, false);
    }

    private static class LibraryAdapter extends FragmentPagerAdapter {
        private ArrayList<Page> pages = new ArrayList<>();

        public LibraryAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        public void addFragment(BaseFragment fragment, String title) {
            pages.add(new Page(fragment, title));
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return pages.get(position).fragment;
        }

        @Override
        public int getCount() {
            return pages.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return pages.get(position).title;
        }

        private static class Page {
            BaseFragment fragment;
            String title;

            public Page(BaseFragment fragment, String title) {
                this.fragment = fragment;
                this.title = title;
            }
        }
    }
}