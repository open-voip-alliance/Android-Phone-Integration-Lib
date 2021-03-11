package org.openvoipalliance.androidplatformintegration

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import org.openvoipalliance.androidplatformintegration.audio.AudioManager
import org.openvoipalliance.androidplatformintegration.call.CallActions
import org.openvoipalliance.androidplatformintegration.call.Calls
import org.openvoipalliance.androidplatformintegration.call.PILCallFactory
import org.openvoipalliance.androidplatformintegration.configuration.ApplicationSetup
import org.openvoipalliance.androidplatformintegration.configuration.Auth
import org.openvoipalliance.androidplatformintegration.configuration.Preferences
import org.openvoipalliance.androidplatformintegration.di.di
import org.openvoipalliance.androidplatformintegration.events.EventsManager
import org.openvoipalliance.androidplatformintegration.events.PILEventListener
import org.openvoipalliance.androidplatformintegration.exception.NoAuthenticationCredentialsException
import org.openvoipalliance.androidplatformintegration.exception.PermissionException
import org.openvoipalliance.androidplatformintegration.helpers.VoIPLibHelper
import org.openvoipalliance.androidplatformintegration.logging.LogLevel
import org.openvoipalliance.androidplatformintegration.telecom.AndroidCallFramework
import org.openvoipalliance.voiplib.VoIPLib
import org.openvoipalliance.voiplib.model.RegistrationState.FAILED
import org.openvoipalliance.voiplib.model.RegistrationState.REGISTERED
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PIL internal constructor(internal val app: ApplicationSetup) {

    private val callFactory: PILCallFactory by di.koin.inject()
    private val androidCallFramework: AndroidCallFramework by di.koin.inject()
    private val phoneLib: VoIPLib by di.koin.inject()
    private val phoneLibHelper: VoIPLibHelper by di.koin.inject()

    val actions: CallActions by di.koin.inject()
    val audio: AudioManager by di.koin.inject()
    val events: EventsManager by di.koin.inject()
    val calls: Calls by di.koin.inject()

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
            phoneLib.register { registrationState ->
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

        start {
            androidCallFramework.placeCall(number)
        }
    }

    /**
     * Start the PIL, unless the force options are provided, the method will not restart or re-register.
     *
     */
    fun start(
        forceInitialize: Boolean = false,
        forceReregister: Boolean = false,
        callback: (() -> Unit)? = null
    ) {
        val auth = auth ?: throw NoAuthenticationCredentialsException()

        if (!auth.isValid) throw NoAuthenticationCredentialsException()

        events.listen(callFactory)

        if (forceInitialize) {
            phoneLib.destroy()
        }

        phoneLibHelper.apply {
            initialise(forceInitialize)
            register(auth, forceReregister) {
                writeLog("The VoIP library has been initialized and the user has been registered!")
                callback?.invoke()
            }
        }
    }

    private fun performPermissionCheck() {
        if (ContextCompat.checkSelfPermission(app.application, Manifest.permission.CALL_PHONE)
            == PackageManager.PERMISSION_DENIED) {
            throw PermissionException(Manifest.permission.CALL_PHONE)
        }
    }

    internal fun writeLog(message: String, level: LogLevel = LogLevel.INFO) {
        app.logger?.onLogReceived(message, level)
    }

    private val isPreparedToStart: Boolean
        get() = auth != null && phoneLib.isInitialised

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
