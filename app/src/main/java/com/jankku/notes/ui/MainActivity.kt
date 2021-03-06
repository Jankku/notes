package com.jankku.notes.ui

import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.preference.PreferenceManager
import com.jankku.notes.R
import java.util.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val themePref = PreferenceManager
            .getDefaultSharedPreferences(applicationContext)
            .getString(getString(R.string.theme_key), null)

        // Set theme
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

        // Set language
        val languagePref = PreferenceManager
            .getDefaultSharedPreferences(applicationContext)
            .getString(getString(R.string.language_key), getString(R.string.language_value_system))
        Log.d("LOG_LANG_PREF", languagePref.toString())
        when (languagePref) {
            getString(R.string.language_value_en) -> updateLanguage(getString(R.string.language_value_en))
            getString(R.string.language_value_fi) -> updateLanguage(getString(R.string.language_value_fi))
            else -> updateLanguage(Locale.getDefault().language)
        }

        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment? ?: return

        // Add note shortcut
        if (intent.action == "com.jankku.notes.addNote") {
            navHostFragment.findNavController()
                .navigate(HomeFragmentDirections.actionHomeFragmentToAddNoteFragment())
            intent.action = "android.intent.action.MAIN"
        }

        setupActionBarWithNavController(navHostFragment.findNavController())
        supportActionBar!!.elevation = 0f
    }

    override fun onSupportNavigateUp(): Boolean =
        findNavController(R.id.nav_host_fragment).navigateUp()

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