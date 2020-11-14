package nl.vialer.voip.android

import android.content.Context
import android.telecom.TelecomManager
import nl.vialer.voip.android.audio.AudioRoute
import nl.vialer.voip.android.audio.AudioState
import nl.vialer.voip.android.configuration.Configuration
import nl.vialer.voip.android.exception.RegistrationFailedException
import nl.vialer.voip.android.logging.LogLevel
import org.openvoipalliance.phonelib.PhoneLib
import org.openvoipalliance.phonelib.config.Auth
import org.openvoipalliance.phonelib.config.Config
import org.openvoipalliance.phonelib.model.Call
import org.openvoipalliance.phonelib.model.RegistrationState
import org.openvoipalliance.phonelib.repository.initialise.CallListener
import org.openvoipalliance.phonelib.repository.initialise.LogLevel as LibraryLogLevel
import org.openvoipalliance.phonelib.repository.initialise.LogLevel.*
import org.openvoipalliance.phonelib.repository.initialise.LogListener

class VoIPPIL(private val config: Configuration, context: Context) : LogListener, CallListener {

    val state = PILState.INACTIVE

    val call: String? = null

    val transferTarget: String? = null

    val audioState = AudioState(AudioRoute.PHONE, false, null)

    internal val phoneLib = PhoneLib.getInstance(context)

    val isMuted = phoneLib.microphoneMuted

    val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager

    fun call(number: String) {

        phoneLib.initialise(Config(
            auth = Auth(config.auth.username, config.auth.password, config.auth.domain, config.auth.port),
            callListener = this,
            encryption = true,
            logListener = this
        ))

        phoneLib.register {
            if (it == RegistrationState.REGISTERED) {
                phoneLib.callTo(number)
            } else {
                throw RegistrationFailedException()
            }
        }

    }

    override fun onLogMessageWritten(lev: LibraryLogLevel, message: String) {
        config.log?.invoke(when(lev) {
            DEBUG -> LogLevel.DEBUG
            TRACE -> LogLevel.DEBUG
            MESSAGE -> LogLevel.INFO
            WARNING -> LogLevel.WARNING
            ERROR -> LogLevel.ERROR
            FATAL -> LogLevel.ERROR
        }, message)
    }

    override fun incomingCallReceived(call: Call) {
        telecomManager.addNewIncomingCall()
    }

    override fun outgoingCallCreated(call: Call) {
//        telecomManager.placeCall()
    }
}