package nl.vialer.voip.android.example.ui.dashboard

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceFragment
import android.text.InputType
import android.util.Log
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import nl.vialer.voip.android.R
import nl.vialer.voip.android.VoIPPIL
import nl.vialer.voip.android.configuration.Auth
import nl.vialer.voip.android.configuration.Configuration

class SettingsFragment : PreferenceFragmentCompat() {

    private val prefs by lazy {
        PreferenceManager.getDefaultSharedPreferences(activity)
    }

    private val voip: VoIPPIL
        get() = VoIPPIL(Configuration(
            auth = Auth(
                prefs.getString("username", "") ?: "",
                prefs.getString("password", "") ?: "",
                prefs.getString("domain", "") ?: "",
                (prefs.getString("port", "0") ?: "0").toInt()
            ),
        ), requireActivity())

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

        arrayOf("username", "password", "domain", "port").forEach {
            findPreference<EditTextPreference>(it)?.setOnPreferenceChangeListener { _, _ ->
                Handler().postDelayed({
                    activity?.runOnUiThread { updateAuthenticationStatus() }
                }, 1000)
                true
            }
        }

        findPreference<Preference>("status")?.setOnPreferenceClickListener {
            updateAuthenticationStatus()
            true
        }
    }

    /**
     * Updates the authentication status field.
     *
     */
    private fun updateAuthenticationStatus() {
        findPreference<Preference>("status")?.summaryProvider = Preference.SummaryProvider<Preference> {
            "Checking authentication..."
        }

        GlobalScope.launch(Dispatchers.IO) {
            val summary = if (voip.canAuthenticate()) "Authenticated" else "Authentication failed"

            activity?.runOnUiThread {
                findPreference<Preference>("status")?.summaryProvider = Preference.SummaryProvider<Preference> {
                    summary
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateAuthenticationStatus()
    }
}