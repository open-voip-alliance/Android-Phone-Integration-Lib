package org.openvoipalliance.androidphoneintegration.helpers

import org.openvoipalliance.androidphoneintegration.PIL
import org.openvoipalliance.androidphoneintegration.call.VoipLibEventTranslator
import org.openvoipalliance.androidphoneintegration.isNullOrInvalid
import org.openvoipalliance.androidphoneintegration.logWithContext
import org.openvoipalliance.androidphoneintegration.logging.LogLevel.ERROR
import org.openvoipalliance.voiplib.VoIPLib
import org.openvoipalliance.voiplib.config.Config
import org.openvoipalliance.voiplib.model.RegistrationState.FAILED
import org.openvoipalliance.voiplib.model.RegistrationState.REGISTERED
import org.openvoipalliance.voiplib.repository.initialise.LogLevel
import org.openvoipalliance.voiplib.repository.initialise.LogListener
import org.openvoipalliance.androidphoneintegration.logging.LogLevel as PilLogLevel

internal class VoIPLibHelper(private val pil: PIL, private val phoneLib: VoIPLib, private val voipLibEventTranslator: VoipLibEventTranslator) {

    /**
     * Boots the VoIP library.
     *
     */
    fun initialise() {
        if (phoneLib.isInitialised) {
            phoneLib.wake()
            log("The VoIP library is already initialized, skipping init.")
            return
        }

        phoneLib.initialise(
            Config(
                callListener = voipLibEventTranslator,
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
    fun register(callback: (Boolean) -> Unit) {
        if (pil.auth.isNullOrInvalid) {
            log("Unable to register as we have no authentication credentials", ERROR)
            callback.invoke(false)
            return
        }

        pil.writeLog("Attempting registration...")

        phoneLib.register { registrationState ->
            when (registrationState) {
                REGISTERED -> {
                    log("Registration was successful!")
                    callback.invoke(true)
                }
                FAILED -> {
                    log("Unable to register...")
                    callback.invoke(false)
                }
                else -> {}
            }
        }
    }

    private fun log(message: String, level: PilLogLevel = PilLogLevel.INFO) =
        logWithContext(message, "PHONE-LIB-HELPER", level)

    private val voipLibraryLogListener = object : LogListener {
        override fun onLogMessageWritten(lev: LogLevel, message: String) {
            pil.app.logger?.onLogReceived(
                message,
                when (lev) {
                    LogLevel.DEBUG -> PilLogLevel.DEBUG
                    LogLevel.TRACE -> PilLogLevel.DEBUG
                    LogLevel.MESSAGE -> PilLogLevel.INFO
                    LogLevel.WARNING -> PilLogLevel.WARNING
                    LogLevel.ERROR -> ERROR
                    LogLevel.FATAL -> ERROR
                }
            )
        }
    }
}
