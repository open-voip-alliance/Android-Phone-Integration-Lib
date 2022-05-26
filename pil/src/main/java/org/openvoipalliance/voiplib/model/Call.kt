package org.openvoipalliance.voiplib.model

import org.linphone.core.Call.Dir.Incoming
import org.linphone.core.Call.Dir.Outgoing
import org.openvoipalliance.voiplib.repository.PreservedInviteData
import org.linphone.core.Call as LinphoneCall

class Call(val linphoneCall: LinphoneCall) {

    val quality
        get() = Quality(linphoneCall.averageQuality, linphoneCall.currentQuality)

    val state
        get() = CallState.values().firstOrNull { it.name == linphoneCall.state.toString() }
                    ?: CallState.Unknown

    val displayName
        get() = linphoneCall.remoteAddress.displayName ?: ""

    val phoneNumber
        get() = linphoneCall.remoteAddress.username ?: ""

    val duration
        get() = linphoneCall.duration

    /**
     * Check if this is a missed call, this will likely not be valid until the call has been
     * released.
     *
     */
    val wasMissed: Boolean
        get() = try {
            val log = linphoneCall.callLog

            val missedStatuses = arrayOf(
                LinphoneCall.Status.Missed,
                LinphoneCall.Status.Aborted,
                LinphoneCall.Status.EarlyAborted,
            )

            log.dir == Incoming && missedStatuses.contains(log.status)
        } catch (e: NullPointerException) {
            false
        }

    val reason
        get() = linphoneCall.reason.name

    val callId: String
        get() = try {
            linphoneCall.callLog.callId ?: ""
        } catch (e: NullPointerException) {
            "unknown"
        }

    val direction
        get() = when (linphoneCall.callLog.dir) {
            Outgoing -> Direction.OUTGOING
            Incoming -> Direction.INCOMING
            null -> Direction.INCOMING
        }

    val remotePartyId: String
        get() {
            val userData = linphoneCall.userData as? PreservedInviteData ?: return ""

            return userData.remotePartyId ?: ""
        }

    val pAssertedIdentity: String
        get() {
            val userData = linphoneCall.userData as? PreservedInviteData ?: return ""

            return userData.pAssertedIdentity ?: ""
        }

    val isOnHold: Boolean
        get() = when (state) {
            CallState.Paused -> true
            else -> false
        }
}