package nl.vialer.voip.android

import com.google.firebase.messaging.FirebaseMessaging
import com.takwolf.android.foreback.Foreback
import nl.vialer.voip.android.android.ApplicationStateListener
import nl.vialer.voip.android.configuration.ApplicationSetup
import nl.vialer.voip.android.configuration.Auth
import nl.vialer.voip.android.configuration.Preferences
import nl.vialer.voip.android.push.Middleware

class Builder {

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
        Foreback.init(pil.application.applicationClass)
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

