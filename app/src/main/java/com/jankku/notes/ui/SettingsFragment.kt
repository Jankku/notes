package com.jankku.notes.ui

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.jankku.notes.R

class SettingsFragment : PreferenceFragmentCompat() {

    private var themePref: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        themePref = findPreference(getString(R.string.theme_header))
        themePref?.onPreferenceChangeListener = themeListener
    }

    private val themeListener =
        Preference.OnPreferenceChangeListener { _, newValue ->
            newValue as? String
            when (newValue) {
                getString(R.string.theme_light) -> updateTheme(MODE_NIGHT_NO)
                getString(R.string.theme_dark) -> updateTheme(MODE_NIGHT_YES)
                else -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        updateTheme(MODE_NIGHT_FOLLOW_SYSTEM)
                    } else {
                        updateTheme(MODE_NIGHT_AUTO_BATTERY)
                    }
                }
            }
        }

    private val themeIconListener =
        Preference.OnPreferenceChangeListener { _, newValue ->
            val isChecked = newValue as? Boolean ?: false
            if (isChecked) {
                themePref?.icon =
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_dark_mode)
            } else {
                themePref?.icon =
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_light_mode)
            }
            true
        }

    private fun updateTheme(nightMode: Int): Boolean {
        setDefaultNightMode(nightMode)
        requireActivity().recreate()
        return true
    }
}