package org.openvoipalliance.androidphoneintegration.example

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.preference.PreferenceManager
import org.openvoipalliance.androidphoneintegration.configuration.ApplicationSetup
import org.openvoipalliance.androidphoneintegration.configuration.Auth
import org.openvoipalliance.androidphoneintegration.example.ui.call.CallActivity
import org.openvoipalliance.androidphoneintegration.example.ui.call.IncomingCallActivity
import org.openvoipalliance.androidphoneintegration.startAndroidPIL

class PILExampleApplication : Application() {

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
                userAgent = "Android-PIL-Example-Application",
                notifyOnMissedCall = true,
                onMissedCallNotificationPressed = {
                    Toast.makeText(this@PILExampleApplication, "Missed call pressed", Toast.LENGTH_LONG).show()
                }
            )
        }
    }
}
