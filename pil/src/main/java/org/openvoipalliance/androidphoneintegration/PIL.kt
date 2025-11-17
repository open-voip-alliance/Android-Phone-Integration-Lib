package org.openvoipalliance.androidphoneintegration

import android.Manifest
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import org.openvoipalliance.androidphoneintegration.android.PlatformIntegrator
import org.openvoipalliance.androidphoneintegration.audio.AudioManager
import org.openvoipalliance.androidphoneintegration.call.*
import org.openvoipalliance.androidphoneintegration.configuration.ApplicationSetup
import org.openvoipalliance.androidphoneintegration.configuration.Auth
import org.openvoipalliance.androidphoneintegration.configuration.Preferences
import org.openvoipalliance.androidphoneintegration.debug.VersionInfo
import org.openvoipalliance.androidphoneintegration.di.di
import org.openvoipalliance.androidphoneintegration.events.Event.CallSetupFailedEvent.OutgoingCallSetupFailed
import org.openvoipalliance.androidphoneintegration.events.Event.CallSetupFailedEvent.Reason.*
import org.openvoipalliance.androidphoneintegration.events.EventsManager
import org.openvoipalliance.androidphoneintegration.exception.NoAuthenticationCredentialsException
import org.openvoipalliance.androidphoneintegration.helpers.VoIPLibHelper
import org.openvoipalliance.androidphoneintegration.logging.LogLevel
import org.openvoipalliance.androidphoneintegration.logging.LogManager
import org.openvoipalliance.androidphoneintegration.notifications.NotificationManager
import org.openvoipalliance.androidphoneintegration.push.TokenFetcher
import org.openvoipalliance.androidphoneintegration.telecom.AndroidCallFramework
import org.openvoipalliance.voiplib.VoIPLib
import org.openvoipalliance.voiplib.config.Config
import org.openvoipalliance.voiplib.model.Codec
import org.openvoipalliance.voiplib.model.RegistrationState.FAILED
import org.openvoipalliance.voiplib.model.RegistrationState.REGISTERED
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PIL internal constructor(internal val app: ApplicationSetup) {
    internal val voipLib: VoIPLib by di.koin.inject()
    internal val phoneLibHelper: VoIPLibHelper by di.koin.inject()

    internal val androidCallFramework: AndroidCallFramework by di.koin.inject()
    internal val platformIntegrator: PlatformIntegrator by di.koin.inject()
    internal val logManager: LogManager by di.koin.inject()
    internal val notifications: NotificationManager by di.koin.inject()
    internal val voipLibEventTranslator: VoipLibEventTranslator by di.koin.inject()
    internal val telephonyManager: TelephonyManager by di.koin.inject()

    val actions: CallActions by di.koin.inject()
    val audio: AudioManager by di.koin.inject()
    val events: EventsManager by di.koin.inject()
    val calls: Calls by di.koin.inject()
    val pushToken = TokenFetcher(app.middleware)

    private val localDtmfToneGenerator: org.openvoipalliance.androidphoneintegration.audio.LocalDtmfToneGenerator by di.koin.inject()

    val sessionState: CallSessionState
        get() = CallSessionState(calls.active, calls.inactive, audio.state)

    var versionInfo: VersionInfo = VersionInfo.build(app.application, voipLib)
        set(value) {
            field = value
            logManager.logVersion()
        }

    var preferences: Preferences = Preferences.DEFAULT
        set(value) {
            val previous = field
            field = value
            if (previous.enableAdvancedLogging != field.enableAdvancedLogging) {
                voipLib.setAppropriateLogLevel()
            }
        }

    var auth: Auth? = null

    init {
        instance = this
        events.listen(platformIntegrator)

        voipLib.initialize(
            Config(
                callListener = voipLibEventTranslator,
                logListener = logManager,
                codecs = arrayOf(Codec.OPUS),
                userAgent = app.userAgent
            )
        )
    }

    /**
     * Place a call to the given number, this will also boot the voip library
     * if it is not already booted.
     */
    fun call(number: String) {
        log("Attempting to make outgoing call")

        if (isEmergencyNumber(number)) {
            log("$number appears to be an emergency number, opening it in the native dialer")
            app.startCallInNativeDialer(number)
            return
        }

        if (androidCallFramework.isInCall) {
            log("Currently in call and so cannot proceed with another", LogLevel.ERROR)
            events.broadcast(OutgoingCallSetupFailed(IN_CALL))
            return
        }

        if (!androidCallFramework.canMakeOutgoingCall) {
            log("Android telecom framework is not permitting outgoing call", LogLevel.ERROR)
            events.broadcast(OutgoingCallSetupFailed(REJECTED_BY_ANDROID_TELECOM_FRAMEWORK))
            return
        }

        androidCallFramework.placeCall(number)
    }

    private fun isEmergencyNumber(number: String) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        telephonyManager.isEmergencyNumber(number)
    } else {
        false
    }

    /**
     * Start the PIL, unless the force options are provided, the method will not restart or re-register.
     */
    @Deprecated("Force parameters no longer used, use new start() method instead.")
    fun start(
        forceInitialize: Boolean = false,
        forceReregister: Boolean = false,
        callback: ((Boolean) -> Unit)? = null
    ) {
        if (!hasRequiredPermissions()) {
            writeLog(
                "Unable to start PIL without required CALL_PHONE permission",
                LogLevel.ERROR
            )
            callback?.invoke(false)
            return
        }

        androidCallFramework.prune()

        if (auth.isNullOrInvalid) throw NoAuthenticationCredentialsException()

        pushToken.request()

        phoneLibHelper.apply {
            register { success ->
                writeLog("The VoIP library has been initialized and the user has been registered!")
                callback?.invoke(success)
            }
        }

        versionInfo = VersionInfo.build(app.application, voipLib)
    }

    fun start(callback: ((Boolean) -> Unit)? = null) =
        start(
            forceInitialize = false,
            forceReregister = false,
            callback = callback,
        )

    /**
     * Stop the PIL, this will remove all authentication credentials from memory and destroy the
     * underlying voip lib. This will not destroy the PIL.
     *
     * This should be called when a user logs-out (or similar action).
     *
     */
    fun stop() {
        auth = null
        voipLib.unregister()
    }

    private fun hasRequiredPermissions() =
        ContextCompat.checkSelfPermission(app.application,
            Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_DENIED

    internal fun writeLog(message: String, level: LogLevel = LogLevel.INFO) {
        logManager.writeLog(message, level)
    }

    private val isPreparedToStart: Boolean
        get() = auth != null && voipLib.isInitialized

    /**
     * Currently this just defers to [isPreparedToStart] as they have the same conditions but this may change in the future.
     */
    internal val isStarted: Boolean
        get() = isPreparedToStart

    fun performEchoCancellationCalibration() {
        log("Beginning echo cancellation calibration")
        voipLib.startEchoCancellerCalibration()
    }

    /**
     * Play a DTMF tone locally without sending it over the network.
     *
     * @param digit The DTMF digit to play ('0'-'9', '#', '*')
     */
    fun playToneLocally(digit: Char) {
        localDtmfToneGenerator.play(digit)
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

/**
 * Log a string with the context (what part of the library the log refers to) appended to the front
 * and formatted correctly.
 */
internal fun logWithContext(message: String, context: String, level: LogLevel = LogLevel.INFO) =
    log("$context: $message", level)

val Auth?.isNullOrInvalid: Boolean
    get() = if (this == null) {
        true
    } else {
        !this.isValid
    }

fun ApplicationSetup.startCallInNativeDialer(number: String) {
    application.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")).apply {
        flags = FLAG_ACTIVITY_NEW_TASK
    })
}