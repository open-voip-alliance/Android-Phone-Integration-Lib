package org.openvoipalliance.androidphoneintegration.call

import org.openvoipalliance.androidphoneintegration.PIL
import org.openvoipalliance.androidphoneintegration.events.Event.CallSessionEvent.*
import org.openvoipalliance.androidphoneintegration.events.Event.CallSessionEvent.AttendedTransferEvent.*
import org.openvoipalliance.androidphoneintegration.events.EventsManager
import org.openvoipalliance.androidphoneintegration.helpers.identifier
import org.openvoipalliance.androidphoneintegration.log
import org.openvoipalliance.voiplib.model.Call
import org.openvoipalliance.voiplib.repository.initialise.CallListener

/**
 * Responsible for taking regular VoipLib events and translating them into a PIL Event.
 *
 */
internal class VoipLibEventTranslator(
    private val pil: PIL,
    private val calls: Calls,
    private val events: EventsManager
) : CallListener {

    override fun incomingCallReceived(call: Call) {
        log("incomingCallReceived: ${call.identifier}")

        if (calls.isInCall) {
            log("Ignoring incoming call (${call.identifier}) as we are in a call already")
            return
        }

        log("Setting up incoming call (${call.identifier}")

        calls.add(call)

        events.broadcast(IncomingCallReceived::class)
    }

    override fun outgoingCallCreated(call: Call) {
        log("outgoingCallCreated: ${call.identifier}")

        calls.add(call)

        if (calls.isInTransfer) {
            events.broadcast(AttendedTransferStarted::class)
        } else {
            events.broadcast(OutgoingCallStarted::class)
        }
    }

    override fun callConnected(call: Call) {
        log("callConnected: ${call.identifier}")

        events.broadcast(
            if (!calls.isInTransfer)
                CallConnected::class
            else
                AttendedTransferConnected::class
        )
    }

    override fun callEnded(call: Call) {
        log("callEnded: ${call.identifier}")

        val currentSessionState = pil.sessionState
        val isInTransfer = calls.isInTransfer

        calls.removeCall(call)

        if (isInTransfer) {
            events.broadcast(AttendedTransferAborted(currentSessionState))
        } else {
            events.broadcast(CallEnded(currentSessionState))
        }
    }

    override fun attendedTransferMerged(call: Call) {
        log("attendedTransferMerged: ${call.identifier}")

        val currentSessionState = pil.sessionState

        calls.removeCall(call)

        events.broadcast(AttendedTransferEnded(currentSessionState))
    }

    override fun callUpdated(call: Call) = events.broadcast(CallStateUpdated::class)

    override fun error(call: Call) = callEnded(call)
}