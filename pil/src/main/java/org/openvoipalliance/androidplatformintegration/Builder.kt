package org.openvoipalliance.androidplatformintegration

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.takwolf.android.foreback.Foreback
import org.openvoipalliance.androidplatformintegration.configuration.ApplicationSetup
import org.openvoipalliance.androidplatformintegration.configuration.Auth
import org.openvoipalliance.androidplatformintegration.configuration.Preferences
import org.openvoipalliance.androidplatformintegration.push.Middleware

class Builder internal constructor() {

    var preferences: Preferences = Preferences.DEFAULT
    var auth: Auth? = null

    internal fun start(applicationSetup: ApplicationSetup): PIL {
        val pil = PIL(applicationSetup)

        applicationSetup.middleware?.let { setupFcmWithMiddleware(it) }
        setupApplicationBackgroundListeners(pil)
        pil.preferences = this.preferences
        auth?.let { pil.auth = it }

        return pil
    }

    private fun setupApplicationBackgroundListeners(pil: PIL) {
        Foreback.init(pil.app.application)
        Foreback.registerListener(ApplicationStateListener(pil))
    }

    private fun setupFcmWithMiddleware(middleware: Middleware) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (!it.isSuccessful) {
                return@addOnCompleteListener
            }

            it.result?.let { token: String ->
                middleware.tokenReceived(token)
            }
        }
    }
}

/**
 * Initialise the Android PIL, this should be called in your Application's onCreate method.
 *
 */
fun startAndroidPIL(init: Builder.() -> ApplicationSetup): PIL {
    val builder = Builder()

    val applicationSetup = init.invoke(builder)

    return builder.start(applicationSetup)
}
