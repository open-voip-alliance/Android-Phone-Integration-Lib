package org.openvoipalliance.androidphoneintegration.example

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.preference.PreferenceManager
import org.openvoipalliance.androidphoneintegration.configuration.ApplicationSetup
import org.openvoipalliance.androidphoneintegration.configuration.Auth
import org.openvoipalliance.androidphoneintegration.example.ui.MissedCallNotificationReceiver
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
            preferences = preferences.copy(
                useApplicationProvidedRingtone = prefs.getBoolean(
                    "use_application_provided_ringtone",
                    false
                )
            )
            auth = if (userAuth.isValid) userAuth else null

            ApplicationSetup(
                application = this@PILExampleApplication,
                activities = ApplicationSetup.Activities(
                    call = CallActivity::class.java,
                    incomingCall = IncomingCallActivity::class.java
                ),
                middleware = VoIPGRIDMiddleware(this@PILExampleApplication),
                logger = { message, _ -> Log.i("PIL-Logger", message) },
                userAgent = "Android-PIL-Example-Application",
                notifyOnMissedCall = true,
                onMissedCallNotificationPressed = PendingIntent.getBroadcast(
                    this@PILExampleApplication,
                    0,
                    Intent(
                        this@PILExampleApplication,
                        MissedCallNotificationReceiver::class.java
                    ).apply {
                        action =
                            MissedCallNotificationReceiver.Action.MISSED_CALL_NOTIFICATION_PRESSED.name
                    },
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
        }
    }
}
