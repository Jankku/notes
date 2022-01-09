package com.jankku.notes.ui

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.preference.PreferenceManager
import com.jankku.notes.R
import com.jankku.notes.databinding.ActivityMainBinding
import com.jankku.notes.util.navigateSafe
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        val themePref = prefs.getString(getString(R.string.theme_key), null)
        when (themePref) {
            getString(R.string.theme_value_light) -> setDefaultNightMode(MODE_NIGHT_NO)
            getString(R.string.theme_value_dark) -> setDefaultNightMode(MODE_NIGHT_YES)
            else -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
                } else {
                    setDefaultNightMode(MODE_NIGHT_AUTO_BATTERY)
                }
            }
        }

        val languagePref = prefs.getString(
            getString(R.string.language_key),
            getString(R.string.language_value_system)
        )
        when (languagePref) {
            getString(R.string.language_value_en) -> updateLanguage(getString(R.string.language_value_en))
            getString(R.string.language_value_fi) -> updateLanguage(getString(R.string.language_value_fi))
            else -> updateLanguage(Locale.getDefault().language)
        }

        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Add note shortcut
        if (intent.action == "${packageName}.addNote") {
            navController.navigateSafe(R.id.action_homeFragment_to_detailFragment)
            intent.action = "android.intent.action.MAIN"
        }

        setSupportActionBar(binding.toolbar)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    fun setCustomTitle(title: String) {
        supportActionBar?.title = title
    }

    // https://stackoverflow.com/questions/38997356/change-language-programmatically-android-n-7-0-api-24
    private fun updateLanguage(language: String): Boolean {
        val locale = Locale(language)
        val configuration = application.resources.configuration

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = LocaleList(locale)

            if (language == getString(R.string.language_value_system)) {
                val defaultLanguage = Locale.getDefault()

                configuration.setLocale(defaultLanguage)
                Locale.setDefault(locale)
                resources.updateConfiguration(configuration, application.resources.displayMetrics)
                application.createConfigurationContext(configuration)
            } else {
                configuration.setLocale(locale)
                configuration.setLocales(localeList)
                Locale.setDefault(locale)
                resources.updateConfiguration(configuration, application.resources.displayMetrics)
                application.createConfigurationContext(configuration)
            }
        } else {
            configuration.setLocale(locale)
            Locale.setDefault(locale)
            resources.updateConfiguration(configuration, application.resources.displayMetrics)
        }

        return true
    }
}