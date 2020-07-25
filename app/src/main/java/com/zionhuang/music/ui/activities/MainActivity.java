package com.zionhuang.music.ui.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.zionhuang.music.R;
import com.zionhuang.music.ui.fragments.BottomControlsFragment;
import com.zionhuang.music.ui.fragments.ExplorationFragment;
import com.zionhuang.music.ui.fragments.LibraryFragment;
import com.zionhuang.music.ui.fragments.SearchResultFragment;
import com.zionhuang.music.ui.fragments.SearchSuggestionFragment;
import com.zionhuang.music.ui.fragments.SettingsFragment;
import com.zionhuang.music.ui.widgets.BottomSheetListener;
import com.zionhuang.music.utils.FragmentNavigator;
import com.zionhuang.music.viewmodels.MainViewModel;

import java.util.ArrayList;

import static com.zionhuang.music.utils.Utils.getDensity;
import static com.zionhuang.music.utils.Utils.replaceFragment;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, FragmentNavigator.FragmentFactory {
    private static final String TAG = "MainActivity";
    private MainViewModel mainViewModel;

    private ArrayList<BottomSheetListener> mBottomSheetCallbacks = new ArrayList<>();
    private boolean isSearching = false;
    private boolean isFocus = false;

    private String query;

    private BottomSheetBehavior<CoordinatorLayout> mBottomSheetBehavior;
    private BottomNavigationView mBottomNavigator;
    private FragmentNavigator mFragmentNavigator;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFragmentNavigator = new FragmentNavigator(savedInstanceState, getSupportFragmentManager(), R.id.nav_host_fragment, this);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        mainViewModel.getSearchQuery().observe(this, query -> searchView.setQuery(query, false));
        mainViewModel.getQuerySubmitListener().observe(this, ignored -> searchView.setQuery(searchView.getQuery(), true));

        if (savedInstanceState == null) {
            mFragmentNavigator.switchTo("library");
        } else {
            if (savedInstanceState.getBoolean("searchBar_isSearching")) {
                isSearching = true;
                isFocus = savedInstanceState.getBoolean("searchBar_isFocus");
                query = savedInstanceState.getString("query");
            }
        }

        setupUI();
    }

    private void setupUI() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        replaceFragment(getSupportFragmentManager(), R.id.bottom_controls_container, new BottomControlsFragment());

        CoordinatorLayout bottomControlsSheet = findViewById(R.id.bottom_controls_sheet);
        mBottomNavigator = findViewById(R.id.bottom_nav);
        mBottomNavigator.setOnNavigationItemSelectedListener(this);

        mBottomSheetBehavior = BottomSheetBehavior.from(bottomControlsSheet);
        mBottomSheetBehavior.setHideable(true);
        mBottomSheetBehavior.addBottomSheetCallback(new BottomSheetCallback());
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (mBottomSheetBehavior != null) {
            mBottomSheetBehavior.setPeekHeight((int) (108 * getDensity(this)), true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = new SearchView(this);
        searchView.setIconifiedByDefault(true);
        if (searchManager != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        }
        searchView.setSubmitButtonEnabled(false);
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                mFragmentNavigator.switchTo("library");
                return true;
            }
        });
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                mBottomNavigator.setVisibility(View.INVISIBLE);
                if (!(mFragmentNavigator.getCurrentFragment() instanceof SearchSuggestionFragment)) {
                    Bundle bundle = new Bundle();
                    bundle.putString("query", searchView.getQuery().toString());
                    mFragmentNavigator.switchTo("suggestion", bundle);
                    //navController.navigate(R.id.navigation_searchSuggestion, bundle, animStack);
                }
            } else {
                mBottomNavigator.setVisibility(View.VISIBLE);
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                mainViewModel.setSearchQuery(newText, false);
//                if (mFragmentNavigator.getCurrentFragment() instanceof SearchSuggestionFragment) {
//                    ((SearchSuggestionFragment) mFragmentNavigator.getCurrentFragment()).fetchSuggestion(newText);
//                }
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                Bundle bundle = new Bundle();
                bundle.putString("query", query);
                mFragmentNavigator.switchTo("search_result", bundle);
                //navController.navigate(R.id.action_searchSuggestion_to_searchResult, bundle);
                return false;
            }
        });
        searchItem.setActionView(searchView);

        if (isSearching) {
            searchItem.expandActionView();
            if (!isFocus) {
                searchView.clearFocus();
            }
            searchView.setQuery(query, false);
        }
        return true;
    }

    public PlayerView getPlayerView() {
        return findViewById(R.id.player_view);
    }

    public void addBottomSheetListener(BottomSheetListener bottomSheetListener) {
        mBottomSheetCallbacks.add(bottomSheetListener);
    }

    public void removeBottomSheetListener(BottomSheetListener bottomSheetListener) {
        mBottomSheetCallbacks.remove(bottomSheetListener);
    }

    private class BottomSheetCallback extends BottomSheetBehavior.BottomSheetCallback {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, @BottomSheetBehavior.State int newState) {
            for (BottomSheetListener bottomSheetListener : mBottomSheetCallbacks) {
                bottomSheetListener.onStateChanged(bottomSheet, newState);
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            if (slideOffset >= 0) {
                mBottomNavigator.setTranslationY(getResources().getDimensionPixelOffset(R.dimen.bottom_navigation_height) * slideOffset);
            }
            for (BottomSheetListener bottomSheetListener : mBottomSheetCallbacks) {
                bottomSheetListener.onSlide(bottomSheet, slideOffset);
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        switch (id) {
            case R.id.navigation_library:
                mFragmentNavigator.switchTo("library");
                break;
            case R.id.navigation_explorarion:
                mFragmentNavigator.switchTo("explore");
                break;
            case R.id.navigation_settings:
                mFragmentNavigator.switchTo("settings");
        }
        return true;
    }

    @Override
    public Fragment createFragment(String tag) {
        switch (tag) {
            case "library":
                return new LibraryFragment();
            case "explore":
                return new ExplorationFragment();
            case "settings":
                return new SettingsFragment();
            case "suggestion":
                return new SearchSuggestionFragment();
            case "search_result":
                return new SearchResultFragment();
        }
        throw new IllegalArgumentException("Unknown fragment tag: " + tag);
    }

    @Override
    protected void onApplyThemeResource(Resources.Theme theme, int resid, boolean first) {
        super.onApplyThemeResource(theme, resid, first);
        // consider api level handle or setting
        Configuration config = getResources().getConfiguration();
        int currentNightMode = config.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_NO) {
            getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("searchBar_isSearching", searchView == null || !searchView.isIconified());
        outState.putBoolean("searchBar_isFocus", searchView != null && searchView.hasFocus());
        if (searchView != null && !searchView.isIconified()) {
            outState.putString("query", searchView.getQuery().toString());
        }
        mFragmentNavigator.writeStateToBundle(outState);
    }
}
