package com.jankku.notes.ui

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.preference.PreferenceManager
import com.jankku.notes.R


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val themePref = PreferenceManager
            .getDefaultSharedPreferences(applicationContext)
            .getString(getString(R.string.theme_key), null)

        // Set theme on activity start
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

        // Add note shortcut
        if ("com.jankku.notes.addNote" == intent.action) {
            navHostFragment.findNavController()
                .navigate(HomeFragmentDirections.actionHomeFragmentToAddNoteFragment())
            intent.action = "android.intent.action.MAIN"
        }

        setupActionBarWithNavController(navHostFragment.findNavController())
        supportActionBar!!.elevation = 0f
    }

    override fun onSupportNavigateUp(): Boolean =
        findNavController(R.id.nav_host_fragment).navigateUp()
}