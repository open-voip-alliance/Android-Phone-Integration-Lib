package org.openvoipalliance.androidphoneintegration

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import org.openvoipalliance.androidphoneintegration.android.PlatformIntegrator
import org.openvoipalliance.androidphoneintegration.audio.AudioManager
import org.openvoipalliance.androidphoneintegration.call.*
import org.openvoipalliance.androidphoneintegration.configuration.ApplicationSetup
import org.openvoipalliance.androidphoneintegration.configuration.Auth
import org.openvoipalliance.androidphoneintegration.configuration.Preferences
import org.openvoipalliance.androidphoneintegration.debug.VersionInfo
import org.openvoipalliance.androidphoneintegration.di.di
import org.openvoipalliance.androidphoneintegration.events.Event
import org.openvoipalliance.androidphoneintegration.events.EventsManager
import org.openvoipalliance.androidphoneintegration.exception.NoAuthenticationCredentialsException
import org.openvoipalliance.androidphoneintegration.exception.PermissionException
import org.openvoipalliance.androidphoneintegration.helpers.VoIPLibHelper
import org.openvoipalliance.androidphoneintegration.logging.LogLevel
import org.openvoipalliance.androidphoneintegration.logging.LogManager
import org.openvoipalliance.androidphoneintegration.notifications.IncomingCallNotification
import org.openvoipalliance.androidphoneintegration.notifications.NotificationManager
import org.openvoipalliance.androidphoneintegration.push.TokenFetcher
import org.openvoipalliance.androidphoneintegration.telecom.AndroidCallFramework
import org.openvoipalliance.voiplib.VoIPLib
import org.openvoipalliance.voiplib.model.RegistrationState.FAILED
import org.openvoipalliance.voiplib.model.RegistrationState.REGISTERED
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PIL internal constructor(internal val app: ApplicationSetup) {

    private val androidCallFramework: AndroidCallFramework by di.koin.inject()
    private val voipLib: VoIPLib by di.koin.inject()
    private val phoneLibHelper: VoIPLibHelper by di.koin.inject()
    private val platformIntegrator: PlatformIntegrator by di.koin.inject()

    internal val logManager: LogManager by di.koin.inject()
    internal val notifications: NotificationManager by di.koin.inject()

    val actions: CallActions by di.koin.inject()
    val audio: AudioManager by di.koin.inject()
    val events: EventsManager by di.koin.inject()
    val calls: Calls by di.koin.inject()
    val pushToken = TokenFetcher(app.middleware)

    val sessionState: CallSessionState
        get() = CallSessionState(calls.active, calls.inactive, audio.state)

    var versionInfo: VersionInfo = VersionInfo.build(app.application, voipLib)
        set(value) {
            field = value
            logManager.logVersion()
        }

    /**
     * The user preferences for the PIL, when this value is updated it will trigger
     * a full PIL restart and re-register.
     *
     */
    var preferences: Preferences = Preferences.DEFAULT
        set(preferences) {
            field = preferences

            if (isPreparedToStart) {
                start(forceInitialize = true, forceReregister = true)
            }
        }

    /**
     * The authentication details for the PIL, when this value is updated it will
     * trigger a full re-register.
     *
     */
    var auth: Auth? = null
        set(auth) {
            field = if (auth?.isValid == true) auth else null

            if (isPreparedToStart) {
                start(forceInitialize = false, forceReregister = true)
            }
        }

    init {
        instance = this
        events.listen(platformIntegrator)
        pushToken.request()
    }

    /**
     * Attempt to boot and register to see if user credentials are correct. This can be used
     * to check the user's credentials after they have supplied them.
     *
     */
    suspend fun performRegistrationCheck(): Boolean = suspendCoroutine { continuation ->
        try {
            if (auth?.isValid == false) {
                continuation.resume(false)
                return@suspendCoroutine
            }

            phoneLibHelper.initialise()
            voipLib.register { registrationState ->
                when (registrationState) {
                    REGISTERED, FAILED -> {
                        continuation.resume(registrationState == REGISTERED)
                    }
                    else -> {
                    }
                }
            }
        } catch (e: Exception) {
            continuation.resume(false)
        }
    }

    /**
     * Place a call to the given number, this will also boot the voip library
     * if it is not already booted.
     *
     */
    fun call(number: String) {
        performPermissionCheck()

        start { success ->
            if (success) {
                androidCallFramework.placeCall(number)
            } else {
                writeLog("Unable to register so not continuing with placing a call", LogLevel.ERROR)
                events.broadcast(Event.CallSetupFailedEvent.OutgoingCallSetupFailed(Event.CallSetupFailedEvent.Reason.UNABLE_TO_REGISTER))
            }
        }
    }

    /**
     * Start the PIL, unless the force options are provided, the method will not restart or re-register.
     *
     */
    fun start(
        forceInitialize: Boolean = false,
        forceReregister: Boolean = false,
        callback: ((Boolean) -> Unit)? = null
    ) {
        pushToken.request()
        
        val auth = auth ?: throw NoAuthenticationCredentialsException()

        if (!auth.isValid) throw NoAuthenticationCredentialsException()

        if (forceInitialize) {
            voipLib.destroy()
        }

        phoneLibHelper.apply {
            initialise(forceInitialize)
            register(auth, forceReregister) { success ->
                writeLog("The VoIP library has been initialized and the user has been registered!")
                callback?.invoke(success)
            }
        }

        versionInfo = VersionInfo.build(app.application, voipLib)
    }

    /**
     * Stop the PIL, this will remove all authentication credentials from memory and destroy the
     * underlying voip lib. This will not destroy the PIL.
     *
     * This should be called when a user logs-out (or similar action).
     *
     */
    fun stop() {
        auth = null
        voipLib.destroy()
    }

    private fun performPermissionCheck() {
        if (ContextCompat.checkSelfPermission(app.application, Manifest.permission.CALL_PHONE)
            == PackageManager.PERMISSION_DENIED) {
            throw PermissionException(Manifest.permission.CALL_PHONE)
        }
    }

    internal fun writeLog(message: String, level: LogLevel = LogLevel.INFO) {
        logManager.writeLog(message, level)
    }

    private val isPreparedToStart: Boolean
        get() = auth != null && voipLib.isInitialised

    companion object {
        lateinit var instance: PIL

        /**
         * Check whether the PIL has been initialized, this should only be needed when calling the PIL
         * from a service class that may run before your application onCreate method.
         *
         */
        val isInitialized: Boolean
            get() = ::instance.isInitialized
    }
}

/**
 * Helper function to write a log from anywhere.
 *
 */
internal fun log(message: String, level: LogLevel = LogLevel.INFO) {
    if (!PIL.isInitialized) return

    PIL.instance.writeLog(message, level)
}