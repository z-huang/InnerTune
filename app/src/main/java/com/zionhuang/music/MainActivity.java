package com.zionhuang.music;

import android.app.SearchManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.SearchView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

public class MainActivity extends AppCompatActivity {
    private boolean isSearching;
    private String query;

    private Youtube youtube;

    private Toolbar toolbar;
    private BottomNavigationView bottomNav;
    private SearchView searchView;
    private MenuItem searchItem;

    private NavController navController;
    public Fragment currentFragment;
    private NavOptions animSwitch, animStack;

    private void setupNavigation(Bundle savedInstanceState) {
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        animStack = new NavOptions.Builder()
                .setEnterAnim(R.anim.fragment_open_enter)
                .setExitAnim(R.anim.fragment_close_exit)
                .setPopEnterAnim(R.anim.nav_default_pop_enter_anim)
                .setPopExitAnim(R.anim.nav_default_pop_exit_anim).build();
        animSwitch = new NavOptions.Builder()
                .setEnterAnim(R.anim.nav_default_enter_anim)
                .setExitAnim(R.anim.nav_default_exit_anim)
                .setPopEnterAnim(R.anim.nav_default_pop_enter_anim)
                .setPopExitAnim(R.anim.nav_default_pop_exit_anim).build();
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.navigation_library:
                        navController.navigate(R.id.navigation_library, null, animSwitch);
                        break;
                    case R.id.navigation_explorarion:
                        navController.navigate(R.id.navigation_explorarion, null, animSwitch);
                        break;
                }
                return true;
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        bottomNav = findViewById(R.id.nav_view);

        NetworkManager.init(this);
        youtube = Youtube.getInstance(this);
        setupNavigation(savedInstanceState);

        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean("searching")) {
                isSearching = true;
                query = savedInstanceState.getString("query");
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("searching", !searchView.isIconified());
        if (!searchView.isIconified()) {
            outState.putString("query", searchView.getQuery().toString());
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
                navController.navigate(R.id.navigation_library, null, animStack);
                return true;
            }
        });
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    bottomNav.setVisibility(View.GONE);
                    if (!(currentFragment instanceof SearchSuggestionFragment)) {
                        Bundle bundle = new Bundle();
                        bundle.putString("query", searchView.getQuery().toString());
                        navController.navigate(R.id.navigation_searchSuggestion, bundle, animStack);
                    }
                } else {
                    bottomNav.setVisibility(View.VISIBLE);
                }
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                if (currentFragment instanceof SearchSuggestionFragment) {
                    ((SearchSuggestionFragment) currentFragment).onQueryTextChange(newText);
                }
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                Bundle bundle = new Bundle();
                bundle.putString("query", query);
                navController.navigate(R.id.action_searchSuggestion_to_searchResult, bundle);
                return false;
            }
        });
        searchItem.setActionView(searchView);

        if (isSearching) {
            searchItem.expandActionView();
            searchView.setQuery(query, false);
        }
        return true;
    }

    public void fillSearchBarQuery(String text) {
        searchView.setQuery(text, false);
    }

    public void search(String text) {
        searchView.setQuery(text, true);
    }

    @Override
    protected void onApplyThemeResource(Resources.Theme theme, int resid, boolean first) {
        super.onApplyThemeResource(theme, resid, first);
        // consider api level handle or setting
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Configuration config = getResources().getConfiguration();
            int currentNightMode = config.uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if (currentNightMode == Configuration.UI_MODE_NIGHT_NO) {
                getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
            }
        }
    }
}
