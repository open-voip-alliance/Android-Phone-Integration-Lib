package nl.vialer.voip.android.example

import android.app.Application
import android.util.Log
import androidx.preference.PreferenceManager
import nl.vialer.voip.android.configuration.ApplicationSetup
import nl.vialer.voip.android.configuration.Auth
import nl.vialer.voip.android.example.ui.call.CallActivity
import nl.vialer.voip.android.example.ui.call.IncomingCallActivity
import nl.vialer.voip.android.startAndroidPIL

class PILExampleApplication: Application() {

    private val prefs by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    override fun onCreate() {
        super.onCreate()

        val username = prefs.getString("username", "") ?: ""
        val password = prefs.getString("password", "") ?: ""
        val domain = prefs.getString("domain", "") ?: ""
        val port = (prefs.getString("port", "0") ?: "0").toInt()

        val userAuth = Auth(
            username = username,
            password = password,
            domain = domain,
            port = port,
            secure = true
        )

        startAndroidPIL {
            preferences = preferences.copy(useApplicationProvidedRingtone = prefs.getBoolean("use_application_provided_ringtone", false))
            auth = if (userAuth.isValid) userAuth else null

            ApplicationSetup(
                application = this@PILExampleApplication,
                activities = ApplicationSetup.Activities(call = CallActivity::class.java, incomingCall = IncomingCallActivity::class.java),
                middleware = VoIPGRIDMiddleware(this@PILExampleApplication),
                logger = { message, _ -> Log.i("PIL-Logger", message) },
                userAgent = "Android-PIL-Example-Application"
            )
        }
    }
}