package nl.vialer.voip.android

import android.telecom.DisconnectCause
import android.telecom.TelecomManager
import nl.vialer.voip.android.events.Event
import nl.vialer.voip.android.service.VoIPService
import nl.vialer.voip.android.service.startCallActivity
import nl.vialer.voip.android.service.startVoipService
import nl.vialer.voip.android.service.stopVoipService
import org.openvoipalliance.phonelib.model.AttendedTransferSession
import org.openvoipalliance.phonelib.model.Call
import org.openvoipalliance.phonelib.repository.initialise.CallListener


internal class CallManager(private val pil: PIL) : CallListener {

    internal var call: Call? = null
    internal var transferSession: AttendedTransferSession? = null

    val isInCall
        get() = this.call != null

    override fun incomingCallReceived(call: Call) {
        if (!isInCall) {
            this.call = call
            pil.connection?.setCallerDisplayName(pil.call?.remotePartyHeading, TelecomManager.PRESENTATION_ALLOWED)
            pil.events.broadcast(Event.INCOMING_CALL_RECEIVED)
            pil.androidTelecomManager.addNewIncomingCall()
        }
    }

    override fun callConnected(call: Call) {
        super.callConnected(call)
        pil.events.broadcast(Event.CALL_CONNECTED)
        pil.app.application.startCallActivity()
    }

    override fun outgoingCallCreated(call: Call) {
        if (!isInCall) {
            this.call = call
            pil.events.broadcast(Event.OUTGOING_CALL_STARTED)
            pil.connection?.setActive()
            pil.connection?.setCallerDisplayName(pil.call?.remotePartyHeading, TelecomManager.PRESENTATION_ALLOWED)
            pil.app.application.startCallActivity()
        }

        if (!VoIPService.isRunning) {
            pil.app.application.startVoipService()
        }
    }

    override fun callEnded(call: Call) {
        pil.writeLog("EVENT RECEIVED: callEnded")

        if (!pil.isInTransfer) {
            this.call = null
            pil.app.application.stopVoipService()
            pil.connection?.setDisconnected(DisconnectCause(DisconnectCause.REMOTE))
            pil.connection?.destroy()
        }

        pil.events.broadcast(Event.CALL_ENDED)
        transferSession = null
    }

    override fun error(call: Call) {
        callEnded(call)
    }
}