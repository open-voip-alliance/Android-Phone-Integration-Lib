package org.openvoipalliance.androidphoneintegration.call

import android.telecom.DisconnectCause
import android.telecom.TelecomManager
import org.openvoipalliance.androidphoneintegration.PIL
import org.openvoipalliance.androidphoneintegration.events.Event
import org.openvoipalliance.androidphoneintegration.service.VoIPService
import org.openvoipalliance.androidphoneintegration.service.startCallActivity
import org.openvoipalliance.androidphoneintegration.service.startVoipService
import org.openvoipalliance.androidphoneintegration.service.stopVoipService
import org.openvoipalliance.androidphoneintegration.telecom.AndroidCallFramework
import org.openvoipalliance.voiplib.model.AttendedTransferSession
import org.openvoipalliance.voiplib.model.Call
import org.openvoipalliance.voiplib.repository.initialise.CallListener

internal class CallManager(private val pil: PIL, private val androidCallFramework: AndroidCallFramework) : CallListener {

    internal var call: Call? = null
    internal var transferSession: AttendedTransferSession? = null

    val isInCall
        get() = this.call != null

    override fun incomingCallReceived(call: Call) {
        if (!isInCall) {
            this.call = call
            pil.events.broadcast(Event.CallEvent.IncomingCallReceived(pil.calls.active))
            androidCallFramework.addNewIncomingCall(call.phoneNumber)
        }
    }

    override fun callConnected(call: Call) {
        super.callConnected(call)
        pil.writeLog("EVENT RECEIVED: callConnected")
        pil.events.broadcast(Event.CallEvent.CallConnected(pil.calls.active))
        pil.app.application.startCallActivity()
    }

    override fun outgoingCallCreated(call: Call) {
        if (!isInCall) {
            this.call = call
            pil.events.broadcast(Event.CallEvent.OutgoingCallStarted(pil.calls.active))
            androidCallFramework.connection?.setActive()
            androidCallFramework.connection?.setCallerDisplayName(
                pil.calls.active?.remotePartyHeading,
                TelecomManager.PRESENTATION_ALLOWED
            )
            pil.app.application.startCallActivity()
        }

        if (!VoIPService.isRunning) {
            pil.app.application.startVoipService()
        }
    }

    override fun callEnded(call: Call) {
        pil.writeLog("EVENT RECEIVED: callEnded")

        if (!pil.calls.isInTransfer) {
            this.call = null
            pil.app.application.stopVoipService()
            androidCallFramework.connection?.setDisconnected(DisconnectCause(DisconnectCause.REMOTE))
            androidCallFramework.connection?.destroy()
        }

        pil.events.broadcast(Event.CallEvent.CallEnded(pil.calls.active))
        transferSession = null
    }

    override fun error(call: Call) {
        callEnded(call)
    }
}
