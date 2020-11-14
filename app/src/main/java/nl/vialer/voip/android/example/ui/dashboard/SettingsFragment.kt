package nl.vialer.voip.android.example.ui.dashboard

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceFragment
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import nl.vialer.voip.android.R

class SettingsFragment : PreferenceFragmentCompat() {

    private val prefs by lazy {
        PreferenceManager.getDefaultSharedPreferences(activity)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
        findPreference<EditTextPreference>("username")?.summaryProvider = Preference.SummaryProvider<EditTextPreference> {
            prefs.getString("username", "")
        }

        findPreference<EditTextPreference>("password")?.apply {
            setOnBindEditTextListener {
                it.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            summaryProvider = Preference.SummaryProvider<EditTextPreference> { prefs.getString("password", "") }
        }

        findPreference<EditTextPreference>("domain")?.apply {
            setOnBindEditTextListener {
                it.inputType = InputType.TYPE_TEXT_VARIATION_URI
            }
            summaryProvider = Preference.SummaryProvider<EditTextPreference> { prefs.getString("domain", "") }
        }

        findPreference<EditTextPreference>("port")?.apply {
            setOnBindEditTextListener {
                it.inputType = InputType.TYPE_CLASS_NUMBER
            }
            summaryProvider = Preference.SummaryProvider<EditTextPreference> { prefs.getString("port", "") }
        }
    }
}