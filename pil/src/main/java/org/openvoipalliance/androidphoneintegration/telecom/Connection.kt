package org.openvoipalliance.androidphoneintegration.telecom

import android.annotation.SuppressLint
import android.telecom.CallAudioState
import android.telecom.DisconnectCause
import android.telecom.DisconnectCause.LOCAL
import org.openvoipalliance.androidphoneintegration.PIL
import org.openvoipalliance.androidphoneintegration.call.CallManager
import org.openvoipalliance.androidphoneintegration.call.VoipLibCall
import org.openvoipalliance.androidphoneintegration.events.Event
import org.openvoipalliance.androidphoneintegration.notifications.IncomingCallNotification
import org.openvoipalliance.voiplib.VoIPLib
import org.openvoipalliance.voiplib.model.Reason
import android.telecom.Connection as AndroidConnection

class Connection internal constructor(
    private val pil: PIL,
    private val phoneLib: VoIPLib,
    private val callManager: CallManager,
    private val androidCallFramework: AndroidCallFramework,
    private val incomingCallNotification: IncomingCallNotification
    ) : AndroidConnection() {

    override fun onShowIncomingCallUi() {
        pil.writeLog("Starting to alert user to an incoming call")
        incomingCallNotification.notify(call = pil.calls.active ?: return)
    }

    override fun onSilence() {
        super.onSilence()
        pil.writeLog("Received request to silence the ringer")
        incomingCallNotification.silence(call = pil.calls.active ?: return)
    }

    override fun onHold() {
        callExists {
            phoneLib.actions(it).hold(true)
            setOnHold()
        }
    }

    fun toggleHold() {
        callExists {
            phoneLib.actions(it).hold(!it.isOnHold)
        }
    }

    override fun onUnhold() {
        callExists {
            phoneLib.actions(it).hold(false)
            setActive()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onAnswer() {
        callExists {
            phoneLib.actions(it).accept()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onReject() {
        callExists {
            phoneLib.actions(it).decline(Reason.BUSY)
            destroy()
        }
    }

    override fun onDisconnect() {
        callExists {
            phoneLib.actions(it).end()
        }

        androidCallFramework.connection = null
        setDisconnected(DisconnectCause(LOCAL))
        destroy()
    }

    override fun onStateChanged(state: Int) {
        super.onStateChanged(state)

        val stateStr = when(state) {
            4 -> "ACTIVE"
            3 -> "DIALING"
            6 -> "DISCONNECTED"
            5 -> "HOLDING"
            0 -> "INITIALIZING"
            1 -> "NEW"
            7 -> "PULLING_CALL"
            2 -> "RINGING"
            else -> "UNKNOWN"
        }

        pil.writeLog("AndroidTelecomFramework State: $stateStr")

        if (state != STATE_RINGING) {
            incomingCallNotification.cancel()
        }
    }

    /**
     * An easy way to perform a null safety check and log whether there was no call found.
     *
     */
    private fun callExists(callback: (voipLibCall: VoipLibCall) -> Unit) {
        var voipLibCall = callManager.voipLibCall ?: return

        callManager.transferSession?.let {
            voipLibCall = it.to
        }

        callback.invoke(voipLibCall)
    }

    override fun onCallAudioStateChanged(state: CallAudioState?) {
        pil.events.broadcast(Event.CallSessionEvent.AudioStateUpdated::class)
    }
}
