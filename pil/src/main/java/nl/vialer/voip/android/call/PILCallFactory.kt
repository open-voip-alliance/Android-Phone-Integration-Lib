package nl.vialer.voip.android.call

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import nl.vialer.voip.android.PIL
import nl.vialer.voip.android.contacts.Contact
import nl.vialer.voip.android.contacts.Contacts
import nl.vialer.voip.android.events.Event
import nl.vialer.voip.android.events.PILEventListener
import org.openvoipalliance.phonelib.model.Call
import org.openvoipalliance.phonelib.model.CallState
import org.openvoipalliance.phonelib.model.CallState.*
import org.openvoipalliance.phonelib.model.Direction
import java.util.*

internal class PILCallFactory(private val pil: PIL, private val contacts: Contacts): PILEventListener {

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

    private fun convertCallState(state: CallState): nl.vialer.voip.android.call.CallState = when(state) {
        Idle, IncomingReceived, OutgoingInit -> nl.vialer.voip.android.call.CallState.INITIALIZING
        OutgoingProgress, OutgoingRinging -> nl.vialer.voip.android.call.CallState.RINGING
        Pausing, Paused -> nl.vialer.voip.android.call.CallState.HELD_BY_LOCAL
        PausedByRemote -> nl.vialer.voip.android.call.CallState.HELD_BY_REMOTE
        OutgoingEarlyMedia, Connected, StreamsRunning, Referred, CallUpdatedByRemote, CallIncomingEarlyMedia,
        CallUpdating, CallEarlyUpdatedByRemote, CallEarlyUpdating, Resuming -> nl.vialer.voip.android.call.CallState.CONNECTED
        Error, Unknown -> nl.vialer.voip.android.call.CallState.ERROR
        CallEnd, CallReleased -> nl.vialer.voip.android.call.CallState.ENDED
    }

    override fun onEvent(event: Event) {
        val call = pil.callManager.call ?: return

        GlobalScope.launch {
            if (!cachedContacts.containsKey(call)) {
                contacts.find(call.phoneNumber)?.also {
                    cachedContacts[call] = it
                }
            }
        }
    }
}