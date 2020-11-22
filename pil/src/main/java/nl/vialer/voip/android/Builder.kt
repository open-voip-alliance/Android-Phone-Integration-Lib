package nl.vialer.voip.android

import android.app.Activity
import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.takwolf.android.foreback.Foreback
import nl.vialer.voip.android.configuration.Auth
import nl.vialer.voip.android.configuration.UI
import nl.vialer.voip.android.exception.NoAuthenticationCredentialsException
import nl.vialer.voip.android.logging.LogCallback
import nl.vialer.voip.android.logging.LogLevel
import nl.vialer.voip.android.push.Middleware
import java.util.logging.Logger

class Builder {

    var callActivity: Class<out Activity>? = null
    var incomingCallActivity: Class<out Activity>? = null
    var token: String? = null
    var middleware: Middleware? = null
    private lateinit var androidApplication: Application
    private var auth: Auth? = null
    private var logger: LogCallback = { _: LogLevel, s: String ->
        Log.i("PIL", s)
    }

    fun androidApplication(application: Application) {
        this.androidApplication = application
    }

    fun logger(logger: LogCallback) {
        this.logger = logger
    }

    fun auth(auth: Auth) {
        this.auth = auth
    }

    internal fun start(): VoIPPIL {
        val voIPPIL = VoIPPIL(
            androidApplication,
            logger
        )

        auth?.let {
            voIPPIL.auth = auth
        }

        voIPPIL.ui = UI(callActivity, incomingCallActivity)

        voIPPIL.middlewareHandler = middleware

        Foreback.init(androidApplication)
        Foreback.registerListener(voIPPIL.androidManager)
        return voIPPIL
    }

    fun middleware(middleware: Middleware) {
        this.middleware = middleware
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (!it.isSuccessful) {
                Log.e("TEST123", "Failed to get token")
                return@addOnCompleteListener
            }

            val token = it.result

            this.token = token

            token?.let {
                middleware.tokenReceived(it)
            }
        }
    }

}

fun startAndroidPIL(init: Builder.() -> Unit) {
    val builder = Builder()

    init.invoke(builder)

    builder.start()
}

