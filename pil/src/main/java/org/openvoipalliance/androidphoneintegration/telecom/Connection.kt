package org.openvoipalliance.androidphoneintegration.telecom

import android.annotation.SuppressLint
import android.telecom.CallAudioState
import android.telecom.DisconnectCause
import org.linphone.core.Reason
import org.openvoipalliance.androidphoneintegration.PIL
import org.openvoipalliance.androidphoneintegration.call.Calls
import org.openvoipalliance.androidphoneintegration.call.VoipLibCall
import org.openvoipalliance.androidphoneintegration.events.Event
import org.openvoipalliance.androidphoneintegration.logWithContext
import org.openvoipalliance.androidphoneintegration.notifications.IncomingCallNotification
import org.openvoipalliance.voiplib.VoIPLib
import android.telecom.Connection as AndroidConnection

class Connection internal constructor(
    private val pil: PIL,
    private val phoneLib: VoIPLib,
    private val incomingCallNotification: IncomingCallNotification,
    private val calls: Calls,
    ) : AndroidConnection() {

    override fun onShowIncomingCallUi() {
        pil.writeLog("Starting to alert user to an incoming call")
        incomingCallNotification.notify(call = pil.calls.active ?: return)
    }

    override fun onSilence() {
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
            if (it.isOnHold) {
                onUnhold()
            } else {
                onHold()
            }
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
            phoneLib.actions(it).decline(Reason.Busy)
            destroy()
        }
    }

    override fun onDisconnect() {
        callExists {
            phoneLib.actions(it).end()
        }
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

        destroyIfNoCallsExist()
    }

    /**
     * An easy way to perform a null safety check and log whether there was no call found.
     *
     */
    private fun callExists(callback: (voipLibCall: VoipLibCall) -> Unit) {
        callback.invoke(calls.activeVoipLibCall ?: return)
            .also { destroyIfNoCallsExist() }
    }

    override fun onCallAudioStateChanged(state: CallAudioState) {
        updateCurrentRouteBasedOnAudioState(state)
        destroyIfNoCallsExist()
    }

    internal fun updateCurrentRouteBasedOnAudioState(state: CallAudioState? = null) =
        calls.activeVoipLibCall?.let {
            val callAudioState = state ?: callAudioState

            log("Updating route based on CallAudioState: [${callAudioState?.route?.asRouteString}]")

            when (callAudioState?.route) {
                CallAudioState.ROUTE_EARPIECE -> phoneLib.actions(it).routeAudioToEarpiece(it)
                CallAudioState.ROUTE_SPEAKER -> phoneLib.actions(it).routeAudioToSpeaker(it)
                CallAudioState.ROUTE_BLUETOOTH -> phoneLib.actions(it).routeAudioToBluetooth(it)
                CallAudioState.ROUTE_WIRED_HEADSET -> phoneLib.actions(it).routeAudioToHeadset(it)
                else -> phoneLib.actions(it).routeAudioToEarpiece(it)
            }

            pil.events.broadcast(Event.CallSessionEvent.AudioStateUpdated::class)
        }

    private fun destroyIfNoCallsExist() {
        if (calls.isInCall) return

        setDisconnected(DisconnectCause(DisconnectCause.UNKNOWN))
        destroy()

        log("Destroying no active call")
    }

    private fun log(message: String) = logWithContext(message, "TELECOM-CONNECTION")
}

private val Int.asRouteString
    get() = when(this) {
        1 -> "ROUTE_EARPIECE"
        2 -> "ROUTE_BLUETOOTH"
        8 -> "ROUTE_SPEAKER"
        4 -> "ROUTE_WIRED_HEADSET"
        5 -> "ROUTE_WIRED_OR_EARPIECE"
        else -> "ROUTE_UNKNOWN"
    }