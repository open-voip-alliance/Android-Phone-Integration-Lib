package org.openvoipalliance.androidphoneintegration.helpers

import org.openvoipalliance.androidphoneintegration.PIL
import org.openvoipalliance.androidphoneintegration.log
import org.openvoipalliance.voiplib.VoIPLib
import org.openvoipalliance.voiplib.config.Auth
import org.openvoipalliance.voiplib.config.Config
import org.openvoipalliance.voiplib.model.RegistrationState
import org.openvoipalliance.voiplib.repository.initialise.LogLevel
import org.openvoipalliance.voiplib.repository.initialise.LogListener
import org.openvoipalliance.androidphoneintegration.logging.LogLevel as PilLogLevel

internal class VoIPLibHelper(private val pil: PIL, private val phoneLib: VoIPLib, private val voipLibEventTranslator: VoipLibEventTranslator) {

    /**
     * Boots the VoIP library.
     *
     */
    fun initialise(forceInitialize: Boolean = false) {
        if (phoneLib.isInitialised && !forceInitialize) {
            phoneLib.wake()
            log("The VoIP library is already initialized, skipping init.")
            return
        }

        val auth = pil.auth ?: run {
            log("There are no authentication credentials, not registering.")
            return
        }

        if (phoneLib.isInitialised && forceInitialize) {
            log("The pil is initialized and we are forcing, so we must destroy it first.")
            phoneLib.destroy()
        }

        phoneLib.initialise(
            Config(
                auth = Auth("", "", "", 0),
                callListener = voipLibEventTranslator,
                encryption = auth.secure,
                logListener = voipLibraryLogListener,
                codecs = pil.preferences.codecs,
                userAgent = pil.app.userAgent
            )
        )
    }

    /**
     * Attempt to register if there are valid credentials.
     *
     *
     */
    fun register(
        auth: org.openvoipalliance.androidphoneintegration.configuration.Auth,
        forceReRegistration: Boolean = false,
        callback: (Boolean) -> Unit
    ) {
        phoneLib.swapConfig(
            phoneLib.currentConfig.copy(
                auth = Auth(
                    auth.username,
                    auth.password,
                    auth.domain,
                    auth.port
                )
            )
        )

        if (phoneLib.isRegistered && !forceReRegistration) {
            pil.writeLog("We are already registered!")
            callback.invoke(true)
            return
        }

        pil.writeLog("Attempting registration...")

        phoneLib.register {
            if (it == RegistrationState.REGISTERED) {
                pil.writeLog("Registration was successful!")
                callback.invoke(true)
                return@register
            }

            if (it == RegistrationState.FAILED) {
                pil.writeLog("Unable to register...")
                callback.invoke(false)
                return@register
            }
        }
    }

    private val voipLibraryLogListener = object : LogListener {
        override fun onLogMessageWritten(lev: LogLevel, message: String) {
            pil.app.logger?.onLogReceived(
                message,
                when (lev) {
                    LogLevel.DEBUG -> PilLogLevel.DEBUG
                    LogLevel.TRACE -> PilLogLevel.DEBUG
                    LogLevel.MESSAGE -> PilLogLevel.INFO
                    LogLevel.WARNING -> PilLogLevel.WARNING
                    LogLevel.ERROR -> PilLogLevel.ERROR
                    LogLevel.FATAL -> PilLogLevel.ERROR
                }
            )
        }
    }
}
