package com.zionhuang.music.ui.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.zionhuang.music.utils.NetworkManager;
import com.zionhuang.music.utils.Player;
import com.zionhuang.music.R;
import com.zionhuang.music.utils.Youtube;
import com.zionhuang.music.ui.exploration.ExplorationFragment;
import com.zionhuang.music.ui.fragments.SearchResultFragment;
import com.zionhuang.music.ui.fragments.SearchSuggestionFragment;
import com.zionhuang.music.ui.library.LibraryFragment;
import com.zionhuang.music.utils.FragmentNavigator;
import com.zionhuang.music.viewmodels.MainViewModel;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, FragmentNavigator.FragmentFactory {
    private MainViewModel mainViewModel;

    private boolean isSearching = false;
    private boolean isFocus = false;

    private String query;

    private Youtube youtube;
    public Player player;

    private Toolbar toolbar;
    private BottomNavigationView mBottomNavigator;
    private FragmentNavigator mFragmentNavigator;
    private SearchView searchView;
    private MenuItem searchItem;


    private TextView miniSongTitle;
    private TextView miniSongArtist;
    private ProgressBar miniProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final Guideline glVideo = findViewById(R.id.guideline_video);
        final ImageView miniPlayPauseBtn = findViewById(R.id.mini_play_pause_btn);

        final PlayerView playerView = findViewById(R.id.player);
        final AspectRatioFrameLayout playerFrame = findViewById(R.id.player_frame);
        Log.d("main", "player width: " + playerFrame.getLayoutParams().width + ", height: " + playerFrame.getLayoutParams().height);

        player = Player.getInstance(this, playerView);
        miniSongTitle = findViewById(R.id.mini_song_title);
        miniSongArtist = findViewById(R.id.mini_song_artist);
        miniProgressBar = findViewById(R.id.player_mini_progress_bar);

        View bottomSheet = findViewById(R.id.bottom_sheet);
        BottomSheetBehavior.from(bottomSheet).addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        int height = ((View) playerFrame.getParent()).getWidth() * 9 / 16;
                        glVideo.setGuidelineBegin(height + 1);
                        miniSongTitle.setTextColor(miniSongTitle.getTextColors().withAlpha(0));
                        miniSongArtist.setTextColor(miniSongArtist.getTextColors().withAlpha(0));
                        miniPlayPauseBtn.setAlpha(0f);
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        miniProgressBar.setVisibility(View.GONE);
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        miniProgressBar.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (slideOffset < 0.5 && slideOffset >= 0) {
                    int fullWidth = ((View) playerFrame.getParent()).getWidth();
                    float fullHeight = ((float) fullWidth) * 9 / 16;
                    float height = 132 + (fullHeight - 132) * slideOffset * 2;
                    glVideo.setGuidelineBegin((int) height);
                    float alpha = 1 - slideOffset * 3;
                    if (alpha < 0) {
                        alpha = 0f;
                    }
                    miniSongTitle.setTextColor(miniSongTitle.getTextColors().withAlpha((int) (255 * alpha)));
                    miniSongArtist.setTextColor(miniSongArtist.getTextColors().withAlpha((int) (255 * alpha)));
                    miniPlayPauseBtn.setAlpha(alpha);
                } else if (slideOffset >= 0.5) {
                    int height = ((View) playerFrame.getParent()).getWidth() * 9 / 16;
                    glVideo.setGuidelineBegin(height + 1);
                    miniSongTitle.setTextColor(miniSongTitle.getTextColors().withAlpha(0));
                    miniSongArtist.setTextColor(miniSongArtist.getTextColors().withAlpha(0));
                    miniPlayPauseBtn.setAlpha(0f);
                }
            }
        });

        NetworkManager.init(this);
        youtube = Youtube.getInstance(this);

        mBottomNavigator = findViewById(R.id.nav_view);
        mBottomNavigator.setOnNavigationItemSelectedListener(this);
        mBottomNavigator.bringToFront();

        mFragmentNavigator = new FragmentNavigator(savedInstanceState, getSupportFragmentManager(), R.id.nav_host_fragment, this);
        if (savedInstanceState == null) {
            mFragmentNavigator.switchTo("library");
        }

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        mainViewModel.getCurrentSong().observe(this, currentSong -> {
            miniSongTitle.setText(currentSong.first);
            miniSongArtist.setText(currentSong.second);
        });

        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean("searchbar_isSearching")) {
                isSearching = true;
                isFocus = savedInstanceState.getBoolean("searchbar_isFocus");
                query = savedInstanceState.getString("query");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchItem = menu.findItem(R.id.action_search);
        searchView = new SearchView(this);
        searchView.setIconifiedByDefault(true);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
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
                mBottomNavigator.setVisibility(View.GONE);
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
                if (mFragmentNavigator.getCurrentFragment() instanceof SearchSuggestionFragment) {
                    ((SearchSuggestionFragment) mFragmentNavigator.getCurrentFragment()).onQueryTextChange(newText);
                }
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
        return findViewById(R.id.player);
    }

    public void fillSearchBarQuery(String text) {
        searchView.setQuery(text, false);
    }

    public void search(String text) {
        searchView.setQuery(text, true);
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
        outState.putBoolean("searchbar_isSearching", searchView == null || !searchView.isIconified());
        outState.putBoolean("searchbar_isFocus", searchView != null && searchView.hasFocus());
        if (!searchView.isIconified()) {
            outState.putString("query", searchView.getQuery().toString());
        }
        mFragmentNavigator.writeStateToBundle(outState);
    }
}
