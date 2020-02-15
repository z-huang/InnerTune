package com.zionhuang.music;

import android.app.SearchManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.SearchView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.zionhuang.music.ui.exploration.ExplorationFragment;
import com.zionhuang.music.ui.library.LibraryFragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;


public class MainActivity extends AppCompatActivity {
    Youtube youtube;
    Toolbar toolbar = null;
    private SearchView searchView = null;
    private MenuItem searchItem = null;
    private NavController navController;
    private FragmentManager fragmentManager;
    private LibraryFragment libraryFragment;
    private ExplorationFragment explorationFragment;
    public Fragment currentFragment;
    private void setupNavigation(Bundle savedInstanceState) {
        fragmentManager = getSupportFragmentManager();
        BottomNavigationView navView = findViewById(R.id.nav_view);
        final NavOptions navOptions = new NavOptions.Builder()
                .setEnterAnim(R.anim.nav_default_enter_anim)
                .setExitAnim(R.anim.nav_default_exit_anim)
                .setPopEnterAnim(R.anim.nav_default_pop_enter_anim)
                .setPopExitAnim(R.anim.nav_default_pop_exit_anim).build();
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment fragment = null;
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
                final FragmentTransaction transaction = fragmentManager.beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.navigation_library:
                        Log.d("navigator", "select library");
                        navController.navigate(R.id.navigation_library, null, navOptions);
                        break;
                    case R.id.navigation_explorarion:
                        Log.d("navigator", "select explore");
                        navController.navigate(R.id.navigation_explorarion, null, navOptions);
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
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_library, R.id.navigation_explorarion, R.id.navigation_notifications)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        //NavigationUI.setupWithNavController(navView, navController);
        NetworkManager.init(this);
        youtube = Youtube.getInstance(this);
        setupNavigation(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        final MainActivity self = this;
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchItem = menu.findItem(R.id.action_search);
        searchView = new SearchView(this);
        searchView.setIconifiedByDefault(true);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(false);
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnSearchClickListener(new SearchView.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.navigation_searchSuggestion);
            }
        });
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                navController.navigate(R.id.navigation_library);
                return true;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("main", "on query text change:" + newText);
                if (currentFragment instanceof SearchSuggestionFragment) {
                    ((SearchSuggestionFragment) currentFragment).onQueryTextChange(newText);
                }
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("main", "on query text submit: " + query);
                navController.navigate(R.id.navigation_searchResult);
                return false;
            }
        });
        searchItem.setActionView(searchView);
        return true;
    }

    public void fillSearchBarQuery(String text) {
        searchView.setQuery(text, false);
    }

    public void search(String text) {
        searchView.setQuery(text, true);
        Bundle bundle = new Bundle();
        bundle.putString("query", text);
        navController.navigate(R.id.navigation_searchResult, bundle);

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

    @Override
    public void onBackPressed() {
//        if (searchView != null && !searchView.isIconified()) {
//            searchView.onActionViewCollapsed();
//            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//            return;
//        }
        super.onBackPressed();
    }
}
