package com.zionhuang.music.ui.activities

import android.content.Context
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.zionhuang.music.R
import com.zionhuang.music.databinding.ActivitySettingsBinding
import com.zionhuang.music.ui.activities.base.ThemedBindingActivity
import com.zionhuang.music.utils.LanguageContextWrapper
import com.zionhuang.music.utils.LocalizationUtils

class SettingsActivity : ThemedBindingActivity<ActivitySettingsBinding>() {
    override fun getViewBinding() = ActivitySettingsBinding.inflate(layoutInflater)

    override fun attachBaseContext(newBase: Context) {
        val locale = LocalizationUtils.getAppLocale(newBase)
        super.attachBaseContext(LanguageContextWrapper.wrap(newBase, locale))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.toolbar.title = destination.label
        }
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean =
        findNavController(R.id.nav_host_fragment).navigateUp() || super.onSupportNavigateUp()
}