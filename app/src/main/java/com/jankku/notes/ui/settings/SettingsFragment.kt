package com.jankku.notes.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.jankku.notes.BuildConfig
import com.jankku.notes.R
import com.jankku.notes.db.NoteDatabase
import com.jankku.notes.util.Constants.EXPORT_REQ_CODE
import com.jankku.notes.util.Constants.IMPORT_REQ_CODE
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var application: Context
    private var exportPref: Preference? = null
    private var importPref: Preference? = null
    private var themePref: Preference? = null
    private var languagePref: Preference? = null
    private var versionPref: Preference? = null
    private var githubPref: Preference? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        application = requireActivity().applicationContext
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        themePref = findPreference(getString(R.string.theme_key))
        themePref?.onPreferenceChangeListener = themeListener

        languagePref = findPreference(getString(R.string.language_key))
        languagePref?.onPreferenceChangeListener = languageListener

        exportPref = findPreference(getString(R.string.export_database_key))
        exportPref?.onPreferenceClickListener = exportListener

        importPref = findPreference(getString(R.string.import_database_key))
        importPref?.onPreferenceClickListener = importListener

        versionPref = findPreference(getString(R.string.app_key))
        versionPref?.summary = BuildConfig.VERSION_NAME

        githubPref = findPreference(getString(R.string.github_key))
        githubPref?.onPreferenceClickListener = githubListener
    }

    private val themeListener =
        Preference.OnPreferenceChangeListener { _, newValue ->
            newValue as? String
            when (newValue) {
                getString(R.string.theme_value_light) -> updateTheme(MODE_NIGHT_NO)
                getString(R.string.theme_value_dark) -> updateTheme(MODE_NIGHT_YES)
                else -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        updateTheme(MODE_NIGHT_FOLLOW_SYSTEM)
                    } else {
                        updateTheme(MODE_NIGHT_AUTO_BATTERY)
                    }
                }
            }
        }

    private val languageListener =
        Preference.OnPreferenceChangeListener { _, newValue ->
            newValue as? String
            when (newValue) {
                getString(R.string.language_value_en) -> {
                    updateLanguage(getString(R.string.language_value_en))
                }
                getString(R.string.language_value_fi) -> {
                    updateLanguage(getString(R.string.language_value_fi))
                }
                else -> updateLanguage(getString(R.string.language_value_system))
            }
        }

    private val githubListener = Preference.OnPreferenceClickListener {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(githubPref?.summary as String?)
        }
        startActivity(intent)
        true
    }

    private val exportListener = Preference.OnPreferenceClickListener {
        exportDBIntent()
        true
    }

    private val importListener = Preference.OnPreferenceClickListener {
        importDBIntent()
        true
    }

    private fun updateTheme(nightMode: Int): Boolean {
        setDefaultNightMode(nightMode)
        requireActivity().recreate()
        return true
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
                requireContext().createConfigurationContext(configuration)
                application.createConfigurationContext(configuration)
            } else {
                configuration.setLocale(locale)
                configuration.setLocales(localeList)
                Locale.setDefault(locale)
                requireContext().createConfigurationContext(configuration)
                application.createConfigurationContext(configuration)
            }
        } else {
            configuration.setLocale(locale)
            Locale.setDefault(locale)
            requireContext().createConfigurationContext(configuration)
        }

        requireActivity().recreate()
        return true
    }


    private fun exportDBIntent() {
        val formattedDate = String.format("%1tY%<tm%<td-%<tH%<tM", Date())
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            type = "application/vnd.sqlite3"
            putExtra(Intent.EXTRA_TITLE, "notes-${formattedDate}.db")
        }
        startActivityForResult(intent, EXPORT_REQ_CODE)
    }

    private fun exportDB(userChosenUri: Uri) {
        try {
            NoteDatabase.destroyInstance()

            val inputStream: FileInputStream =
                requireActivity().getDatabasePath(NoteDatabase.DATABASE_NAME).inputStream()
            val outputStream = requireActivity().contentResolver.openOutputStream(userChosenUri)

            inputStream.use { input ->
                outputStream.use { output ->
                    if (output != null) {
                        input.copyTo(output)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun importDBIntent() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        }
        startActivityForResult(intent, IMPORT_REQ_CODE)
    }

    private fun importDB(userChosenUri: Uri) {
        NoteDatabase.destroyInstance()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val dbPath = requireActivity().getDatabasePath(NoteDatabase.DATABASE_NAME)
                val inputStream = requireActivity().contentResolver.openInputStream(userChosenUri)
                val outputStream = FileOutputStream(dbPath)

                val data = inputStream?.readBytes() ?: return@launch
                outputStream.write(data, 0, data.size)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == EXPORT_REQ_CODE && resultCode == Activity.RESULT_OK) {
            val userChosenUri = data?.data ?: return

            exportDB(userChosenUri)
            Toast.makeText(context, R.string.export_database_successful, Toast.LENGTH_LONG)
                .show()
        } else if (requestCode == IMPORT_REQ_CODE && resultCode == Activity.RESULT_OK) {
            val userChosenUri = data?.data ?: return

            importDB(userChosenUri)
            Toast.makeText(context, R.string.import_database_successful, Toast.LENGTH_LONG)
                .show()
        }
    }
}