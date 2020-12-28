package org.openvoipalliance.androidplatformintegration

import org.openvoipalliance.phonelib.config.Auth
import org.openvoipalliance.phonelib.config.Config
import org.openvoipalliance.phonelib.model.RegistrationState
import org.openvoipalliance.phonelib.repository.initialise.LogLevel
import org.openvoipalliance.phonelib.repository.initialise.LogListener
import org.openvoipalliance.androidplatformintegration.logging.LogLevel as PilLogLevel

internal class PhoneLibHelper(private val pil: PIL) {

    /**
     * Boots the VoIP library.
     *
     */
    fun initialise(forceInitialize: Boolean = false) {
        if (pil.phoneLib.isInitialised && !forceInitialize) {
            pil.phoneLib.wake()
            pil.writeLog("The VoIP library is already initialised, skipping init.")
            return
        }

        val auth = pil.auth ?: run {
            pil.writeLog("There are no authentication credentials, not registering.")
            return
        }

        pil.phoneLib.initialise(
            Config(
                auth = Auth("", "", "", 0),
                callListener = pil.callManager,
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
        auth: org.openvoipalliance.androidplatformintegration.configuration.Auth,
        forceReRegistration: Boolean = false,
        callback: () -> Unit
    ) {
        pil.phoneLib.swapConfig(
            pil.phoneLib.currentConfig.copy(
                auth = Auth(
                    auth.username,
                    auth.password,
                    auth.domain,
                    auth.port
                )
            )
        )

        if (pil.phoneLib.isRegistered && !forceReRegistration) {
            pil.writeLog("We are already registered!")
            callback.invoke()
            return
        }

        pil.writeLog("Attempting registration...")

        pil.phoneLib.register {
            if (it == RegistrationState.REGISTERED) {
                pil.writeLog("Registration was successful!")
                callback.invoke()
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
