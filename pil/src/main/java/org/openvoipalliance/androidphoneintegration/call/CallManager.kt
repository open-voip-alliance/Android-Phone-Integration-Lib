package org.openvoipalliance.androidphoneintegration.call

import android.telecom.DisconnectCause
import android.telecom.TelecomManager
import org.openvoipalliance.androidphoneintegration.PIL
import org.openvoipalliance.androidphoneintegration.events.Event
import org.openvoipalliance.androidphoneintegration.helpers.startCallActivity
import org.openvoipalliance.androidphoneintegration.helpers.startVoipService
import org.openvoipalliance.androidphoneintegration.helpers.stopVoipService
import org.openvoipalliance.androidphoneintegration.service.VoIPService
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
        pil.writeLog("Incoming call has been received")

        if (!isInCall) {
            pil.writeLog("There is no active call so setting up our new incoming call")
            this.call = call
            pil.events.broadcast(Event.CallEvent.IncomingCallReceived(pil.calls.active))
            androidCallFramework.addNewIncomingCall(call.phoneNumber)
        }
    }

    override fun callConnected(call: Call) {
        super.callConnected(call)
        pil.writeLog("A call has connected!")
        pil.events.broadcast(Event.CallEvent.CallConnected(pil.calls.active))
        pil.app.application.startCallActivity()

        if (!VoIPService.isRunning) {
            pil.writeLog("The VoIP service is not running, starting it.")
            pil.app.application.startVoipService()
        }
    }

    override fun outgoingCallCreated(call: Call) {
        pil.writeLog("An outgoing call has been created")

        if (!isInCall) {
            pil.writeLog("There is no active call yet so we will setup our outgoing call")
            this.call = call
            pil.events.broadcast(Event.CallEvent.OutgoingCallStarted(pil.calls.active))
            androidCallFramework.connection?.setActive()
            androidCallFramework.connection?.setCallerDisplayName(
                pil.calls.active?.remotePartyHeading,
                TelecomManager.PRESENTATION_ALLOWED
            )
            pil.app.application.startCallActivity()
        }
    }

    override fun callEnded(call: Call) {
        pil.writeLog("Call has ended")

        if (!pil.calls.isInTransfer) {
            pil.writeLog("We are not in a call so tearing down VoIP")
            this.call = null
            pil.app.application.stopVoipService()
            androidCallFramework.connection?.setDisconnected(DisconnectCause(DisconnectCause.REMOTE))
            androidCallFramework.connection?.destroy()
        }

        pil.events.broadcast(Event.CallEvent.CallEnded(pil.calls.active))
        transferSession = null
    }

    override fun error(call: Call) {
        pil.writeLog("There was an error, sending a call ended event.")
        callEnded(call)
    }
}
