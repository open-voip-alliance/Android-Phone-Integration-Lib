package nl.vialer.voip.android.example

import android.app.Application
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.util.Log
import androidx.preference.PreferenceManager
import nl.vialer.voip.android.VoIPPIL

import nl.vialer.voip.android.configuration.Auth
import nl.vialer.voip.android.events.Event
import nl.vialer.voip.android.events.Event.*
import nl.vialer.voip.android.events.EventListener
import nl.vialer.voip.android.example.ui.VoIPGRIDMiddleware
import nl.vialer.voip.android.example.ui.call.CallActivity
import nl.vialer.voip.android.example.ui.call.IncomingCallActivity
import nl.vialer.voip.android.startAndroidPIL

class PILExampleApplication: Application(), EventListener {

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

        VoIPPIL.instance.events.listen(this)
    }

    override fun onEvent(event: Event) {
        when (event) {
            OUTGOING_CALL_STARTED, CALL_CONNECTED -> startActivity(Intent(this, CallActivity::class.java).apply { flags = FLAG_ACTIVITY_NEW_TASK })
            else -> {}
        }
    }
}