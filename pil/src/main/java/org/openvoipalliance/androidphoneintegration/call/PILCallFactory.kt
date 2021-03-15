package org.openvoipalliance.androidphoneintegration.call

import java.util.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.openvoipalliance.androidphoneintegration.contacts.Contact
import org.openvoipalliance.androidphoneintegration.contacts.Contacts
import org.openvoipalliance.androidphoneintegration.events.Event
import org.openvoipalliance.androidphoneintegration.events.PILEventListener
import org.openvoipalliance.voiplib.model.Call
import org.openvoipalliance.voiplib.model.CallState
import org.openvoipalliance.voiplib.model.CallState.*
import org.openvoipalliance.voiplib.model.Direction

internal class PILCallFactory(private val contacts: Contacts, private val callManager: CallManager) :
    PILEventListener {

    private val cachedContacts = mutableMapOf<Call, Contact>()

    fun make(libraryCall: Call?): PILCall? {
        val call = libraryCall ?: return null

        return PILCall(
            call.phoneNumber,
            call.displayName,
            convertCallState(call.state),
            if (call.direction == Direction.INCOMING) CallDirection.INBOUND else CallDirection.OUTBOUND,
            call.duration,
            call.isOnHold,
            UUID.randomUUID().toString(),
            call.quality.average,
            cachedContacts[call]
        )
    }

    private fun convertCallState(state: CallState): org.openvoipalliance.androidphoneintegration.call.CallState = when (state) {
        Idle, IncomingReceived, OutgoingInit -> org.openvoipalliance.androidphoneintegration.call.CallState.INITIALIZING
        OutgoingProgress, OutgoingRinging -> org.openvoipalliance.androidphoneintegration.call.CallState.RINGING
        Pausing, Paused -> org.openvoipalliance.androidphoneintegration.call.CallState.HELD_BY_LOCAL
        PausedByRemote -> org.openvoipalliance.androidphoneintegration.call.CallState.HELD_BY_REMOTE
        OutgoingEarlyMedia, Connected, StreamsRunning, Referred, CallUpdatedByRemote, CallIncomingEarlyMedia,
        CallUpdating, CallEarlyUpdatedByRemote, CallEarlyUpdating, Resuming -> org.openvoipalliance.androidphoneintegration.call.CallState.CONNECTED
        Error, Unknown -> org.openvoipalliance.androidphoneintegration.call.CallState.ERROR
        CallEnd, CallReleased -> org.openvoipalliance.androidphoneintegration.call.CallState.ENDED
    }

    override fun onEvent(event: Event) {
        val call = callManager.call ?: return

        GlobalScope.launch {
            if (!cachedContacts.containsKey(call)) {
                contacts.find(call.phoneNumber)?.also {
                    cachedContacts[call] = it
                }
            }
        }
    }
}
