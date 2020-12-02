package nl.vialer.voip.android

import android.Manifest
import android.content.pm.PackageManager
import android.telecom.TelecomManager
import androidx.core.content.ContextCompat
import nl.vialer.voip.android.audio.AudioManager
import nl.vialer.voip.android.call.CallActions
import nl.vialer.voip.android.call.PILCall
import nl.vialer.voip.android.call.PILCallFactory
import nl.vialer.voip.android.configuration.ApplicationSetup
import nl.vialer.voip.android.configuration.Auth
import nl.vialer.voip.android.configuration.Preferences
import nl.vialer.voip.android.contacts.Contacts
import nl.vialer.voip.android.events.PILEventListener
import nl.vialer.voip.android.events.EventsManager
import nl.vialer.voip.android.exception.NoAuthenticationCredentialsException
import nl.vialer.voip.android.exception.PermissionException
import nl.vialer.voip.android.logging.LogLevel
import nl.vialer.voip.android.telecom.AndroidTelecomManager
import nl.vialer.voip.android.telecom.Connection
import org.openvoipalliance.phonelib.PhoneLib
import org.openvoipalliance.phonelib.model.Codec
import org.openvoipalliance.phonelib.model.RegistrationState.FAILED
import org.openvoipalliance.phonelib.model.RegistrationState.REGISTERED
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PIL internal constructor(internal val application: ApplicationSetup) {

    private val callFactory = PILCallFactory(this, Contacts(application.applicationClass))
    internal var connection: Connection? = null
    internal val callManager = CallManager(this)
    internal val androidTelecomManager: AndroidTelecomManager = AndroidTelecomManager(application.applicationClass, application.applicationClass.getSystemService(TelecomManager::class.java))
    internal val phoneLib by lazy { PhoneLib.getInstance(application.applicationClass) }
    private val phoneLibHelper = PhoneLibHelper(this)

    val actions = CallActions(this, phoneLib, callManager)
    val audio = AudioManager(this, phoneLib, callManager)
    val events = EventsManager(this)

    var preferences: Preferences = Preferences.DEFAULT
        set(auth) {
            field = auth
            start(forceInitialize = true, forceReregister = true)
        }

    var auth: Auth? = null
        set(auth) {
            field = auth
            start(forceInitialize = false, forceReregister = true)
        }

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

    init {
        instance = this

        arrayOf<PILEventListener>(callFactory).forEach {
            events.listen(it)
        }
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
            androidTelecomManager.placeCall(number)
        }
    }

    /**
     * Start the PIL, unless the force options are provided, the method will not restart or re-register.
     *
     */
    fun start(forceInitialize: Boolean = false, forceReregister: Boolean = false, callback: (() -> Unit)? = null) {
        val auth = auth ?: throw NoAuthenticationCredentialsException()

        if (forceInitialize) {
            phoneLib.destroy()
        }

        phoneLibHelper.apply {
            initialise(forceInitialize)
            register(auth, forceReregister) {
                writeLog("The VoIP library has been initialised and the user has been registered!")
                callback?.invoke()
            }
        }
    }

    val availableCodecs = arrayOf(Codec.OPUS)

    private fun performPermissionCheck() {
        if (ContextCompat.checkSelfPermission(application.applicationClass, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_DENIED) {
            throw PermissionException(Manifest.permission.CALL_PHONE)
        }
    }

    internal fun writeLog(message: String, level: LogLevel = LogLevel.INFO) {
        application.logger?.onLogReceived(message, level)
    }

    companion object {
        lateinit var instance: PIL
    }
}