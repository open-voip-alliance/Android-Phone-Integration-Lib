package org.openvoipalliance.androidphoneintegration.voip.linphone

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.linphone.core.Call
import org.linphone.core.Core
import org.openvoipalliance.androidphoneintegration.PIL
import org.openvoipalliance.androidphoneintegration.call.Calls
import org.openvoipalliance.androidphoneintegration.events.Event
import org.openvoipalliance.androidphoneintegration.events.Event.CallSessionEvent.AttendedTransferEvent.AttendedTransferStarted
import org.openvoipalliance.androidphoneintegration.events.Event.CallSessionEvent.IncomingCallReceived
import org.openvoipalliance.androidphoneintegration.events.Event.CallSessionEvent.OutgoingCallStarted
import org.openvoipalliance.androidphoneintegration.events.EventsManager
import org.openvoipalliance.androidphoneintegration.log

class LinphoneListener(private val pil: PIL,
                       private val calls: Calls,
                       private val events: EventsManager
) : SimpleCoreListener {


    @DelicateCoroutinesApi
    override fun onCallStateChanged(lc: Core, linphoneCall: Call, state: Call.State, message: String) {
        GlobalScope.launch(Dispatchers.Main) {
            log("callState: $state, Message: $message, Duration = ${linphoneCall.duration}")

            preserveInviteData(linphoneCall)

            handle(state, linphoneCall)
        }
    }

    private fun handle(state: Call.State, call: Call) = when (state) {
        Call.State.IncomingReceived -> {
            log("incomingCallReceived: ${call.identifier}")

            if (calls.isInCall) {
                log("Ignoring incoming call (${call.identifier}) as we are in a call already")
            } else {
                log("Setting up incoming call (${call.identifier}")

                calls.add(call)

                events.broadcast(IncomingCallReceived::class)
            }
        }
        Call.State.OutgoingInit -> {
            log("outgoingCallCreated: ${call.identifier}")

            calls.add(call)

            if (calls.isInTransfer) {
                events.broadcast(AttendedTransferStarted::class)
            } else {
                events.broadcast(OutgoingCallStarted::class)
            }

        }
        Call.State.Connected -> {
            safeLinphoneCore?.activateAudioSession(true)

            log("callConnected: ${call.identifier}")

            events.broadcast(
                if (!calls.isInTransfer)
                    Event.CallSessionEvent.CallConnected::class
                else
                    Event.CallSessionEvent.AttendedTransferEvent.AttendedTransferConnected::class
            )
        }
        Call.State.End, Call.State.Error -> {
            log("callEnded: ${call.identifier}")

            val currentSessionState = pil.sessionState
            val isInTransfer = calls.isInTransfer

            calls.removeCall(call)

            if (isInTransfer) {
                events.broadcast(Event.CallSessionEvent.AttendedTransferEvent.AttendedTransferAborted(
                    currentSessionState))
            } else {
                events.broadcast(Event.CallSessionEvent.CallEnded(currentSessionState))
            }
        }
        Call.State.Released -> pil.platformIntegrator.notifyIfMissedCall(call)
        else -> events.broadcast(Event.CallSessionEvent.CallStateUpdated::class)
    }

    override fun onTransferStateChanged(lc: Core, transfered: Call, newCallState: Call.State) {
        log("attendedTransferMerged: ${transfered.identifier}")

        val currentSessionState = pil.sessionState

        calls.removeCall(transfered)

        events.broadcast(Event.CallSessionEvent.AttendedTransferEvent.AttendedTransferEnded(
            currentSessionState))
    }

    /**
     * When placing a call on hold, certain INVITE information is lost,
     * this will ensure that the first value for a given call is preserved
     * so it is always available, even after being put on hold.
     *
     * The data is only updated when a new, non-null, non-blank value
     * is found.
     */
    private fun preserveInviteData(linphoneCall: Call) {
        if (linphoneCall.userData == null) {
            linphoneCall.userData = PreservedInviteData()
        }

        val userData = linphoneCall.userData as? PreservedInviteData ?: return

        userData.pAssertedIdentity = linphoneCall.remoteParams?.getCustomHeader("P-Asserted-Identity")
        userData.remotePartyId = linphoneCall.remoteParams?.getCustomHeader("Remote-Party-ID")
    }
}

/**
 * Preserves certain information that is not necessarily
 * carried between call objects.
 */
internal class PreservedInviteData {
    var remotePartyId: String? = ""
        set(value) {
            if (value.isNullOrBlank()) return

            field = value
        }

    var pAssertedIdentity: String? = ""
        set(value) {
            if (value.isNullOrBlank()) return

            field = value
        }
}