package com.jankku.notes

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.preference.PreferenceManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val themePref = PreferenceManager
            .getDefaultSharedPreferences(applicationContext)
            .getString(getString(R.string.theme_header), null)

        // Set theme on app start
        when (themePref) {
            getString(R.string.theme_light) -> setDefaultNightMode(MODE_NIGHT_NO)
            getString(R.string.theme_dark) -> setDefaultNightMode(MODE_NIGHT_YES)
            else -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
                } else {
                    setDefaultNightMode(MODE_NIGHT_AUTO_BATTERY)
                }
            }
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment? ?: return
        setupActionBarWithNavController(navHostFragment.findNavController())
    }

    override fun onSupportNavigateUp(): Boolean = findNavController(R.id.nav_host_fragment).navigateUp()
}