package org.openvoipalliance.voiplib.model

import org.linphone.core.Call.Dir.Incoming
import org.linphone.core.Call.Dir.Outgoing
import org.linphone.core.Reason
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
        get() {
            val log = linphoneCall.callLog

            val missedStatuses = arrayOf(
                LinphoneCall.Status.Missed,
                LinphoneCall.Status.Aborted,
                LinphoneCall.Status.EarlyAborted,
            )

            return log.dir == Incoming && missedStatuses.contains(log.status)
        }

    val reason
        get() = when (linphoneCall.reason) {
                Reason.None -> org.openvoipalliance.voiplib.model.Reason.NONE
                Reason.NoResponse -> org.openvoipalliance.voiplib.model.Reason.NO_RESPONSE
                Reason.Declined -> org.openvoipalliance.voiplib.model.Reason.DECLINED
                Reason.NotFound -> org.openvoipalliance.voiplib.model.Reason.NOT_FOUND
                Reason.NotAnswered -> org.openvoipalliance.voiplib.model.Reason.NOT_ANSWERED
                Reason.Busy -> org.openvoipalliance.voiplib.model.Reason.BUSY
                Reason.IOError -> org.openvoipalliance.voiplib.model.Reason.IO_ERROR
                Reason.DoNotDisturb -> org.openvoipalliance.voiplib.model.Reason.DO_NOT_DISTURB
                Reason.Unauthorized -> org.openvoipalliance.voiplib.model.Reason.UNAUTHORISED
                Reason.NotAcceptable -> org.openvoipalliance.voiplib.model.Reason.NOT_ACCEPTABLE
                Reason.NoMatch -> org.openvoipalliance.voiplib.model.Reason.NO_MATCH
                Reason.MovedPermanently -> org.openvoipalliance.voiplib.model.Reason.MOVED_PERMANENTLY
                Reason.Gone -> org.openvoipalliance.voiplib.model.Reason.GONE
                Reason.TemporarilyUnavailable -> org.openvoipalliance.voiplib.model.Reason.TEMPORARILY_UNAVAILABLE
                Reason.AddressIncomplete -> org.openvoipalliance.voiplib.model.Reason.ADDRESS_INCOMPLETE
                Reason.NotImplemented -> org.openvoipalliance.voiplib.model.Reason.NOT_IMPLEMENTED
                Reason.BadGateway -> org.openvoipalliance.voiplib.model.Reason.BAD_GATEWAY
                Reason.ServerTimeout -> org.openvoipalliance.voiplib.model.Reason.SERVER_TIMEOUT
                Reason.Unknown -> org.openvoipalliance.voiplib.model.Reason.UNKNOWN
                else -> org.openvoipalliance.voiplib.model.Reason.UNKNOWN
            }

    val callId: String?
        get() = linphoneCall.callLog.callId

    val direction = when (linphoneCall.callLog.dir) {
        Outgoing -> Direction.OUTGOING
        Incoming -> Direction.INCOMING
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