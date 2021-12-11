package org.openvoipalliance.androidphoneintegration.call

import org.openvoipalliance.androidphoneintegration.contacts.Contact
import org.openvoipalliance.androidphoneintegration.contacts.Contacts
import org.openvoipalliance.androidphoneintegration.helpers.identifier
import org.openvoipalliance.voiplib.model.CallState
import org.openvoipalliance.voiplib.model.CallState.*
import org.openvoipalliance.voiplib.model.Direction
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

typealias VoipLibCall = org.openvoipalliance.voiplib.model.Call

internal class CallFactory(private val contacts: Contacts) {

    private val cachedContacts = mutableMapOf<String, Contact?>()

    fun make(voipLibCall: VoipLibCall?): Call? {
        val call = voipLibCall ?: return null
        val remoteParty = findAppropriateRemotePartyInformation(call)

        return Call(
            remoteParty.number,
            remoteParty.name,
            convertCallState(voipLibCall.state),
            if (voipLibCall.direction == Direction.INCOMING) CallDirection.INBOUND else CallDirection.OUTBOUND,
            call.duration,
            call.isOnHold,
            UUID.randomUUID().toString(),
            call.quality.average,
            findContact(voipLibCall),
            voipLibCall.callId,
            voipLibCall.reason,
        )
    }

    private fun findContact(voipLibCall: VoipLibCall): Contact? {
        if (cachedContacts.contains(voipLibCall.identifier)) {
            return cachedContacts[voipLibCall.identifier]
        }

        return contacts.find(voipLibCall.phoneNumber).also {
            cachedContacts[voipLibCall.identifier] = it
        }
    }

    private fun findAppropriateRemotePartyInformation(call: VoipLibCall): RemotePartyInformation {
        if (call.pAssertedIdentity.isNotBlank()) {
            extractCallerInformationFromAlternativeHeaders(call.pAssertedIdentity)?.let {
                return it
            }
        }

        if (call.remotePartyId.isNotBlank()) {
            extractCallerInformationFromAlternativeHeaders(call.remotePartyId)?.let {
                return it
            }
        }

        return RemotePartyInformation(call.displayName, call.phoneNumber)
    }

    private fun convertCallState(state: CallState): org.openvoipalliance.androidphoneintegration.call.CallState = when (state) {
        Idle, IncomingReceived, OutgoingInit -> org.openvoipalliance.androidphoneintegration.call.CallState.INITIALIZING
        OutgoingProgress, OutgoingRinging -> org.openvoipalliance.androidphoneintegration.call.CallState.RINGING
        Pausing, Paused -> org.openvoipalliance.androidphoneintegration.call.CallState.HELD_BY_LOCAL
        PausedByRemote -> org.openvoipalliance.androidphoneintegration.call.CallState.HELD_BY_REMOTE
        OutgoingEarlyMedia, Connected, StreamsRunning, Referred, CallUpdatedByRemote, CallIncomingEarlyMedia,
        CallUpdating, CallEarlyUpdatedByRemote, CallEarlyUpdating, Resuming,
        -> org.openvoipalliance.androidphoneintegration.call.CallState.CONNECTED
        Error, Unknown -> org.openvoipalliance.androidphoneintegration.call.CallState.ERROR
        CallEnd, CallReleased -> org.openvoipalliance.androidphoneintegration.call.CallState.ENDED
    }

    private fun extractCallerInformationFromAlternativeHeaders(header: String): RemotePartyInformation? {
        val data: List<String> = header.extractCaptureGroups("^\"(.+)\" <?sip:(.+)@")

        return if (data.size < 2) null else RemotePartyInformation(data[0], data[1])
    }

}

internal data class RemotePartyInformation(
    val name: String,
    val number: String
)

internal fun String.extractCaptureGroups(pattern: String): List<String> {
    val p: Pattern = Pattern.compile(pattern)
    val m: Matcher = p.matcher(this)

    if (!m.find()) {
        return ArrayList()
    }

    val matches: ArrayList<String> = ArrayList()

    var i = 1

    while (true) {
        try {
            val match: String = m.group(i) ?: return matches
            matches.add(match)
            i++
        } catch (e: Exception) {
            return matches
        }
    }

}