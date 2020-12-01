package nl.vialer.voip.android

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.telecom.TelecomManager
import android.util.Log
import com.takwolf.android.foreback.Foreback
import nl.vialer.voip.android.audio.AudioManager
import nl.vialer.voip.android.call.CallActions
import nl.vialer.voip.android.call.PILCall
import nl.vialer.voip.android.call.PILCallFactory
import nl.vialer.voip.android.configuration.Auth
import nl.vialer.voip.android.configuration.UI
import nl.vialer.voip.android.contacts.Contacts
import nl.vialer.voip.android.events.Event
import nl.vialer.voip.android.events.EventListener
import nl.vialer.voip.android.events.EventsManager
import nl.vialer.voip.android.exception.NoAuthenticationCredentialsException
import nl.vialer.voip.android.logging.LogCallback
import nl.vialer.voip.android.logging.LogLevel
import nl.vialer.voip.android.push.Middleware
import nl.vialer.voip.android.telecom.AndroidTelecomManager
import nl.vialer.voip.android.telecom.Connection
import org.openvoipalliance.phonelib.PhoneLib
import org.openvoipalliance.phonelib.config.Config
import org.openvoipalliance.phonelib.model.Codec
import org.openvoipalliance.phonelib.model.RegistrationState.FAILED
import org.openvoipalliance.phonelib.model.RegistrationState.REGISTERED
import org.openvoipalliance.phonelib.repository.initialise.LogListener
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.properties.Delegates

class VoIPPIL internal constructor(internal val context: Context, internal val logger: LogCallback) {

    internal var connection: Connection? = null
    internal val callManager = CallManager(this)
    internal val logManager = LogManager(this)
    internal val androidManager = AndroidManager(this)
    internal val callFactory = PILCallFactory(this, Contacts(context))
    internal val androidTelecomManager: AndroidTelecomManager = AndroidTelecomManager(context, context.getSystemService(TelecomManager::class.java))

    val call: PILCall?
        get() = run {
            if (isInTransfer) {
                return@run callFactory.make(callManager.transferSession?.to)
            }

            callManager.call?.let { callFactory.make(it) }
        }

    val transferCall: PILCall?
        get() = callManager.transferSession?.let { callFactory.make(it.from) }

    val isInTransfer: Boolean
        get() = callManager.transferSession != null


    internal var auth: Auth? = null
    internal lateinit var ui: UI

    internal val phoneLib by lazy { PhoneLib.getInstance(context) }

    val actions = CallActions(this, phoneLib, callManager)
    val audio = AudioManager(this, phoneLib, callManager)
    val events = EventsManager(this)
    var middlewareHandler: Middleware? = null

    init {
        instance = this

        arrayOf<EventListener>(callFactory).forEach {
            events.listen(it)
        }
    }

    /**
     * Attempts to authenticate with the server to verify whether or not
     * authentication credentials are correct.
     *
     */
    suspend fun canAuthenticate(): Boolean = suspendCoroutine { continuation ->
        try {
            if (auth?.isValid == false) {
                continuation.resume(false)
                return@suspendCoroutine
            }

            initialise()
            phoneLib.register { registrationState ->
                when (registrationState) {
                    REGISTERED, FAILED -> {
                        continuation.resume(registrationState == REGISTERED)
                        phoneLib.destroy()
                    }
                    else -> {
                    }
                }
            }
        } catch (e: Exception) {
            continuation.resume(false)
        }
    }

    @SuppressLint("MissingPermission")
    fun call(number: String) {
        initialise()

        register {
            androidTelecomManager.placeCall(number)
        }
    }

    internal fun initialise() {
        if (phoneLib.isInitialised) {
            Log.e("TEST123", "isInitialised nothing")
            return
        }

        auth?.let {
            phoneLib.initialise(
                Config(
                    auth = org.openvoipalliance.phonelib.config.Auth(
                        it.username,
                        it.password,
                        it.domain,
                        it.port
                    ),
                    callListener = callManager,
                    encryption = it.secure,
                    logListener = logManager,
                    codecs = arrayOf(Codec.OPUS)
                )
            )
        }
    }

    internal fun register(callback: () -> Unit) {
        if (auth?.isValid == false) {
            writeLog("Unable to register without valid authentication", LogLevel.ERROR)
            return
        }

        if (phoneLib.isRegistered) {
            Log.e("TEST123", "isReady nothing")
            callback.invoke()
            return
        }

        phoneLib.register {
            if (it == REGISTERED) {
                callback.invoke()
            }
        }
    }

    internal fun writeLog(message: String, level: LogLevel = LogLevel.INFO) {
        logger.invoke(level, message)
    }

    fun start(callback: (() -> Unit)? = null) {
        initialise()

        auth?.let {
            register {
                writeLog("We have started the voip pil and registered!")
                callback?.invoke()
            }
        }
    }

    companion object {
        lateinit var instance: VoIPPIL
    }
}