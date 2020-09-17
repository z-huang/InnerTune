package com.zionhuang.music.ui.activities;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.zionhuang.music.R;
import com.zionhuang.music.ui.fragments.BottomControlsFragment;
import com.zionhuang.music.ui.widgets.BottomSheetListener;

import java.util.Objects;

import static com.zionhuang.music.utils.Utils.getDensity;
import static com.zionhuang.music.utils.Utils.replaceFragment;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private BottomSheetListener mBottomSheetCallback;
    private BottomSheetBehavior<CoordinatorLayout> mBottomSheetBehavior;
    private BottomNavigationView mBottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupUI();
    }

    private void setupUI() {
        mBottomNav = findViewById(R.id.bottom_nav);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = Objects.requireNonNull(navHostFragment).getNavController();
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.libraryFragment, R.id.explorationFragment, R.id.settingsFragment).build();
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(mBottomNav, navController);

        replaceFragment(getSupportFragmentManager(), R.id.bottom_controls_container, new BottomControlsFragment());

        CoordinatorLayout bottomControlsSheet = findViewById(R.id.bottom_controls_sheet);
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

    public void setBottomSheetListener(BottomSheetListener bottomSheetListener) {
        mBottomSheetCallback = bottomSheetListener;
    }

    private class BottomSheetCallback extends BottomSheetBehavior.BottomSheetCallback {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, @BottomSheetBehavior.State int newState) {
            if (mBottomSheetCallback != null) {
                mBottomSheetCallback.onStateChanged(bottomSheet, newState);
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            if (slideOffset >= 0) {
                mBottomNav.setTranslationY(getResources().getDimensionPixelOffset(R.dimen.bottom_navigation_height) * slideOffset);
            }
            if (mBottomSheetCallback != null) {
                mBottomSheetCallback.onSlide(bottomSheet, slideOffset);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onApplyThemeResource(Resources.Theme theme, int resId, boolean first) {
        super.onApplyThemeResource(theme, resId, first);
        // TODO: make this a setting option
        Configuration config = getResources().getConfiguration();
        int currentNightMode = config.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_NO) {
            getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        }
    }
}
