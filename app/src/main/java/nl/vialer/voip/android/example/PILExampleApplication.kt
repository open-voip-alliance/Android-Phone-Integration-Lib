package nl.vialer.voip.android.example

import android.app.Application
import android.util.Log
import androidx.preference.PreferenceManager

import nl.vialer.voip.android.configuration.Auth
import nl.vialer.voip.android.example.ui.VoIPGRIDMiddleware
import nl.vialer.voip.android.example.ui.call.CallActivity
import nl.vialer.voip.android.example.ui.call.IncomingCallActivity
import nl.vialer.voip.android.startAndroidPIL

class PILExampleApplication: Application() {

    private val prefs
        get() = PreferenceManager.getDefaultSharedPreferences(this)


    override fun onCreate() {
        super.onCreate()

        val username = prefs.getString("username", "") ?: ""
        val password = prefs.getString("password", "") ?: ""
        val domain = prefs.getString("domain", "") ?: ""
        val port = (prefs.getString("port", "0") ?: "0").toInt()

        startAndroidPIL {
            callActivity = CallActivity::class.java
            incomingCallActivity = IncomingCallActivity::class.java
            androidApplication(this@PILExampleApplication)
            middleware(VoIPGRIDMiddleware(this@PILExampleApplication))
            auth(
                Auth(username, password, domain, port, secure = true)
            )
        }
    }
}