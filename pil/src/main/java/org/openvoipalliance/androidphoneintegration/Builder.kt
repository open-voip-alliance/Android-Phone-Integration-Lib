package org.openvoipalliance.androidphoneintegration

import androidx.lifecycle.ProcessLifecycleOwner
import org.openvoipalliance.androidphoneintegration.configuration.ApplicationSetup
import org.openvoipalliance.androidphoneintegration.configuration.Auth
import org.openvoipalliance.androidphoneintegration.configuration.Preferences
import org.openvoipalliance.androidphoneintegration.di.initPilKoin
import org.openvoipalliance.androidphoneintegration.exception.PILAlreadyInitializedException

class Builder internal constructor() {

    var preferences: Preferences = Preferences.DEFAULT
    var auth: Auth? = null

    internal fun start(applicationSetup: ApplicationSetup): PIL {
        if (PIL.isInitialized) {
            throw PILAlreadyInitializedException()
        }

        initPilKoin(applicationSetup.application)

        val pil = PIL(applicationSetup)

        setupApplicationBackgroundListeners(pil)
        pil.preferences = this.preferences
        auth?.let { pil.auth = it }

        return pil
    }

    private fun setupApplicationBackgroundListeners(pil: PIL) {
        ProcessLifecycleOwner.get().lifecycle.addObserver(ApplicationStateListener(pil))
    }
}

/**
 * Initialize the Android PIL, this should be called in your Application's onCreate method.
 *
 */
fun startAndroidPIL(init: Builder.() -> ApplicationSetup): PIL {
    val builder = Builder()

    val applicationSetup = init.invoke(builder)

    return builder.start(applicationSetup)
}
