package org.openvoipalliance.androidplatformintegration.call

import com.google.android.gms.tasks.Tasks.call
import java.util.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.openvoipalliance.androidplatformintegration.CallManager
import org.openvoipalliance.androidplatformintegration.PIL
import org.openvoipalliance.androidplatformintegration.contacts.Contact
import org.openvoipalliance.androidplatformintegration.contacts.Contacts
import org.openvoipalliance.androidplatformintegration.events.Event
import org.openvoipalliance.androidplatformintegration.events.PILEventListener
import org.openvoipalliance.phonelib.model.Call
import org.openvoipalliance.phonelib.model.CallState
import org.openvoipalliance.phonelib.model.CallState.*
import org.openvoipalliance.phonelib.model.Direction

internal class PILCallFactory(private val pil: PIL, private val contacts: Contacts, private val callManager: CallManager) :
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

    private fun convertCallState(state: CallState): org.openvoipalliance.androidplatformintegration.call.CallState = when (state) {
        Idle, IncomingReceived, OutgoingInit -> org.openvoipalliance.androidplatformintegration.call.CallState.INITIALIZING
        OutgoingProgress, OutgoingRinging -> org.openvoipalliance.androidplatformintegration.call.CallState.RINGING
        Pausing, Paused -> org.openvoipalliance.androidplatformintegration.call.CallState.HELD_BY_LOCAL
        PausedByRemote -> org.openvoipalliance.androidplatformintegration.call.CallState.HELD_BY_REMOTE
        OutgoingEarlyMedia, Connected, StreamsRunning, Referred, CallUpdatedByRemote, CallIncomingEarlyMedia,
        CallUpdating, CallEarlyUpdatedByRemote, CallEarlyUpdating, Resuming -> org.openvoipalliance.androidplatformintegration.call.CallState.CONNECTED
        Error, Unknown -> org.openvoipalliance.androidplatformintegration.call.CallState.ERROR
        CallEnd, CallReleased -> org.openvoipalliance.androidplatformintegration.call.CallState.ENDED
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
