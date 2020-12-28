package org.openvoipalliance.androidplatformintegration.example.ui.settings

import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.widget.Toast
import androidx.preference.*
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.openvoipalliance.androidplatformintegration.PIL
import org.openvoipalliance.androidplatformintegration.configuration.Auth
import org.openvoipalliance.androidplatformintegration.example.R
import org.openvoipalliance.androidplatformintegration.example.VoIPGRIDMiddleware

class SettingsFragment : PreferenceFragmentCompat() {

    private val prefs by lazy {
        PreferenceManager.getDefaultSharedPreferences(activity)
    }

    private val pil by lazy { PIL.instance }

    private val voIPGRIDMiddleware by lazy { VoIPGRIDMiddleware(requireActivity()) }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
        findPreference<EditTextPreference>("username")?.summaryProvider = Preference.SummaryProvider<EditTextPreference> {
            prefs.getString("username", "")
        }

        findPreference<Preference>("voipgrid_middleware_token")?.summaryProvider = Preference.SummaryProvider<Preference> {
            VoIPGRIDMiddleware.token
        }

        findPreference<EditTextPreference>("voipgrid_username")?.summaryProvider = Preference.SummaryProvider<EditTextPreference> {
            prefs.getString("voipgrid_username", "")
        }

        findPreference<EditTextPreference>("password")?.apply {
            setOnBindEditTextListener {
                it.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            summaryProvider = Preference.SummaryProvider<EditTextPreference> {
                prefs.getString(
                    "password",
                    ""
                )
            }
        }

        findPreference<EditTextPreference>("voipgrid_password")?.apply {
            setOnBindEditTextListener {
                it.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            summaryProvider = Preference.SummaryProvider<EditTextPreference> {
                prefs.getString(
                    "voipgrid_password",
                    ""
                )
            }
        }

        findPreference<EditTextPreference>("domain")?.apply {
            setOnBindEditTextListener {
                it.inputType = InputType.TYPE_TEXT_VARIATION_URI
            }
            summaryProvider = Preference.SummaryProvider<EditTextPreference> {
                prefs.getString(
                    "domain",
                    ""
                )
            }
        }

        findPreference<EditTextPreference>("port")?.apply {
            setOnBindEditTextListener {
                it.inputType = InputType.TYPE_CLASS_NUMBER
            }
            summaryProvider = Preference.SummaryProvider<EditTextPreference> {
                prefs.getString(
                    "port",
                    ""
                )
            }
        }

        arrayOf("username", "password", "domain", "port").forEach {
            findPreference<EditTextPreference>(it)?.setOnPreferenceChangeListener { _, _ ->
                Handler().postDelayed(
                    {
                        activity?.runOnUiThread { updateAuthenticationStatus() }
                    },
                    1000
                )
                true
            }
        }

        arrayOf("voipgrid_username", "voipgrid_password").forEach {
            findPreference<EditTextPreference>(it)?.setOnPreferenceChangeListener { _, _ ->
                Handler().postDelayed(
                    {
                        activity?.runOnUiThread { updateVoipgridAuthenticationStatus() }
                    },
                    1000
                )
                true
            }
        }

        findPreference<Preference>("status")?.setOnPreferenceClickListener {
            updateAuthenticationStatus()
            true
        }

        findPreference<Preference>("voipgrid_middleware_register")?.setOnPreferenceClickListener {
            GlobalScope.launch {
                val message = if (voIPGRIDMiddleware.register()) "Registered!" else "Registration failed..."
                requireActivity().runOnUiThread { Toast.makeText(requireActivity(), message, Toast.LENGTH_LONG).show() }
            }
            true
        }

        findPreference<Preference>("voipgrid_middleware_unregister")?.setOnPreferenceClickListener {
            GlobalScope.launch {
                val message = if (voIPGRIDMiddleware.unregister()) "Unregistered!" else "Unregistration failed..."
                requireActivity().runOnUiThread { Toast.makeText(requireActivity(), message, Toast.LENGTH_LONG).show() }
            }
            true
        }

        findPreference<SwitchPreferenceCompat>("use_application_provided_ringtone")?.setOnPreferenceChangeListener { _, newValue ->
            PIL.instance.preferences = PIL.instance.preferences.copy(useApplicationProvidedRingtone = newValue as Boolean)
            true
        }
    }

    private fun updateVoipgridAuthenticationStatus() {
        val queue = Volley.newRequestQueue(requireActivity())

        val url = "https://partner.voipgrid.nl/api/permission/apitoken/"

        val requestData = JSONObject().apply {
            put("email", prefs.getString("voipgrid_username", ""))
            put("password", prefs.getString("voipgrid_password", ""))
        }

        val request = JsonObjectRequest(
            Request.Method.POST,
            url,
            requestData,
            { response ->
                val apiToken = response.getString("api_token")
                updateVoipgridSummary(true, apiToken)
                prefs.edit().putString("voipgrid_api_token", apiToken).apply()
            },
            { error ->
                Toast.makeText(
                    requireContext(),
                    error.networkResponse.statusCode.toString(),
                    Toast.LENGTH_LONG
                ).show()
                updateVoipgridSummary(false)
                prefs.edit().remove("voipgrid_api_token").apply()
            }
        )

        queue.add(request)
    }

    private fun updateVoipgridSummary(authenticated: Boolean, token: String? = null) {
        val summary = if (authenticated) "Authenticated ($token)" else "Authentication failed"

        activity?.runOnUiThread {
            findPreference<Preference>("voipgrid_status")?.summaryProvider = Preference.SummaryProvider<Preference> {
                summary
            }

            findPreference<Preference>("voipgrid_middleware_register")?.isEnabled = authenticated
            findPreference<Preference>("voipgrid_middleware_unregister")?.isEnabled = authenticated
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

        val username = prefs.getString("username", "") ?: ""
        val password = prefs.getString("password", "") ?: ""
        val domain = prefs.getString("domain", "") ?: ""
        val port = (prefs.getString("port", "0") ?: "0").toInt()

        if (username.isNotBlank() && password.isNotBlank() && domain.isNotBlank() && port != 0) {
            pil.auth = Auth(
                username = username,
                password = password,
                domain = domain,
                port = port,
                secure = true
            )
        }

        GlobalScope.launch(Dispatchers.IO) {
            val summary = if (pil.performRegistrationCheck()) "Authenticated" else "Authentication failed"

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
        updateVoipgridAuthenticationStatus()
    }
}
