package nl.vialer.voip.android.example

import android.app.Application
import android.util.Log
import nl.vialer.voip.android.configuration.ApplicationSetup
import nl.vialer.voip.android.example.ui.call.CallActivity
import nl.vialer.voip.android.example.ui.call.IncomingCallActivity
import nl.vialer.voip.android.startAndroidPIL

class PILExampleApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        startAndroidPIL {
            ApplicationSetup(
                applicationClass = this@PILExampleApplication,
                activities = ApplicationSetup.Activities(call = CallActivity::class.java, incomingCall = IncomingCallActivity::class.java),
                middleware = VoIPGRIDMiddleware(this@PILExampleApplication),
                logger = { message, _ -> Log.i("PIL-Logger", message) }
            )
        }
    }
}