package nl.vialer.voip.android

import android.annotation.SuppressLint
import android.content.Context
import android.telecom.DisconnectCause
import android.telecom.TelecomManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.vialer.voip.android.audio.AudioRoute
import nl.vialer.voip.android.audio.AudioState
import nl.vialer.voip.android.call.CallDirection
import nl.vialer.voip.android.call.CallState
import nl.vialer.voip.android.call.PILCall
import nl.vialer.voip.android.configuration.Configuration
import nl.vialer.voip.android.events.Event
import nl.vialer.voip.android.events.EventListener
import nl.vialer.voip.android.exception.RegistrationFailedException
import nl.vialer.voip.android.logging.LogLevel
import nl.vialer.voip.android.telecom.AndroidTelecomManager
import nl.vialer.voip.android.telecom.Connection
import org.openvoipalliance.phonelib.PhoneLib
import org.openvoipalliance.phonelib.config.Auth
import org.openvoipalliance.phonelib.config.Config
import org.openvoipalliance.phonelib.model.Call
import org.openvoipalliance.phonelib.model.Codec
import org.openvoipalliance.phonelib.model.Direction
import org.openvoipalliance.phonelib.model.RegistrationState
import org.openvoipalliance.phonelib.model.RegistrationState.*
import org.openvoipalliance.phonelib.repository.initialise.CallListener
import org.openvoipalliance.phonelib.repository.initialise.LogLevel as LibraryLogLevel
import org.openvoipalliance.phonelib.repository.initialise.LogLevel.*
import org.openvoipalliance.phonelib.repository.initialise.LogListener
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class VoIPPIL(private var config: Configuration, private val context: Context) {

    val state = PILState.INACTIVE

    val call: PILCall?
        get() {
            if (phoneLibCall == null) return null

            return PILCall(
                phoneLibCall!!.phoneNumber,
                phoneLibCall!!.displayName,
                CallState.CONNECTED,
                if (phoneLibCall!!.direction == Direction.INCOMING) CallDirection.INBOUND else CallDirection.OUTBOUND,
                phoneLibCall!!.duration,
                false,
                UUID.randomUUID().toString(),
                phoneLibCall!!.quality.average
            )
        }

    val transferTarget: String? = null

    val audioState
        get() = internalAudioState ?: AudioState(AudioRoute.PHONE, arrayOf(AudioRoute.PHONE, AudioRoute.SPEAKER), null)

    internal var internalAudioState: AudioState? = null

    internal val phoneLib = PhoneLib.getInstance(context)

    val isMuted
        get() = phoneLib.microphoneMuted

    internal val telecomManager = AndroidTelecomManager(context, context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager)

    internal var connection: Connection? = null

    internal var phoneLibCall: Call? = null

    var eventListener: EventListener? = null

    init {
        Log.e("TEST123", "aeasdasd")
        instance = this
    }

    @SuppressLint("MissingPermission")
    fun call(number: String) {

        telecomManager.placeCall(number)


    }

    /**
     * Attempts to authenticate with the server to verify whether or not
     * authentication credentials are correct.
     *
     */
    suspend fun canAuthenticate(): Boolean = suspendCoroutine { continuation ->
        try {
            initialiseWithConfig()
            phoneLib.register { registrationState ->
                when (registrationState) {
                    REGISTERED, FAILED -> {
                        continuation.resume(registrationState == REGISTERED)
                        phoneLib.destroy()
                    }
                    else -> {}
                }
            }
        } catch (e: Exception) {
            continuation.resume(false)
        }
    }

    fun endCall() {
        connection?.let { it.onDisconnect() }
    }

    fun swapConfig(callback: (currentConfig: Configuration) -> Configuration) {
        this.config = callback.invoke(this.config)
    }

    internal fun initialiseWithConfig() {
        Log.e("TEST123", "Initialising... on ${Thread.currentThread()}")
        phoneLib.initialise(Config(
            auth = Auth(config.auth.username, config.auth.password, config.auth.domain, config.auth.port),
            callListener = listener,
            encryption = true,
            logListener = listener,
            codecs = arrayOf(Codec.OPUS)
        ))
    }

    internal fun broadcast(event: Event) {
        Log.e("TEST123", "Broadcasting ${event.name}")
        eventListener?.invoke(event)
    }

    internal object listener : LogListener, CallListener {

        override fun onLogMessageWritten(lev: LibraryLogLevel, message: String) {
            Log.i("LINPHONE", message)
//            config.log?.invoke(when(lev) {
//                DEBUG -> LogLevel.DEBUG
//                TRACE -> LogLevel.DEBUG
//                MESSAGE -> LogLevel.INFO
//                WARNING -> LogLevel.WARNING
//                ERROR -> LogLevel.ERROR
//                FATAL -> LogLevel.ERROR
//            }, message)
        }

        override fun incomingCallReceived(call: Call) {
            if (instance.phoneLibCall == null) {
                instance.telecomManager.addNewIncomingCall()
                instance.phoneLibCall = call
            }
        }

        override fun outgoingCallCreated(call: Call) {
            if (instance.phoneLibCall == null) {
                instance.connection?.let {
                    Log.e("TEST123", "Setting connection to active")
                    it.setActive()
                }
                instance.phoneLibCall = call
                instance.broadcast(Event.OUTGOING_CALL_STARTED)
            }
        }

        override fun callEnded(call: Call) {
            instance.connection?.let {
                Log.e("TEST123", "Got connection, destroying it!")
                it.setDisconnected(DisconnectCause(DisconnectCause.REMOTE))
                it.destroy()
            }
            instance.connection = null
            instance.phoneLibCall = null
            instance.broadcast(Event.CALL_ENDED)
            instance.phoneLib.destroy()
            instance.context.stopVoipService()
        }
    }

    companion object {
        lateinit var instance: VoIPPIL
    }
}