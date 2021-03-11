package org.openvoipalliance.androidplatformintegration.helpers

import org.openvoipalliance.androidplatformintegration.PIL
import org.openvoipalliance.androidplatformintegration.call.CallManager
import org.openvoipalliance.voiplib.VoIPLib
import org.openvoipalliance.voiplib.config.Auth
import org.openvoipalliance.voiplib.config.Config
import org.openvoipalliance.voiplib.model.RegistrationState
import org.openvoipalliance.voiplib.repository.initialise.LogLevel
import org.openvoipalliance.voiplib.repository.initialise.LogListener
import org.openvoipalliance.androidplatformintegration.logging.LogLevel as PilLogLevel

internal class VoIPLibHelper(private val pil: PIL, private val phoneLib: VoIPLib, private val callManager: CallManager) {

    /**
     * Boots the VoIP library.
     *
     */
    fun initialise(forceInitialize: Boolean = false) {
        if (phoneLib.isInitialised && !forceInitialize) {
            phoneLib.wake()
            pil.writeLog("The VoIP library is already initialized, skipping init.")
            return
        }

        val auth = pil.auth ?: run {
            pil.writeLog("There are no authentication credentials, not registering.")
            return
        }

        phoneLib.initialise(
            Config(
                auth = Auth("", "", "", 0),
                callListener = callManager,
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
            callback.invoke()
            return
        }

        pil.writeLog("Attempting registration...")

        phoneLib.register {
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
