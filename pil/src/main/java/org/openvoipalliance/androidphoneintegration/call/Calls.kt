package org.openvoipalliance.androidphoneintegration.call

import org.openvoipalliance.voiplib.model.AttendedTransferSession
import org.openvoipalliance.voiplib.model.Call as VoipLibCall


class Calls internal constructor(private val factory: CallFactory, private val list: CallList)
    : CallList by list {

    internal val isInCall
        get() = list.isNotEmpty()

    val isInTransfer: Boolean
        get() = list.size >= 2

    internal val activeVoipLibCall: VoipLibCall?
        get() = list.lastOrNull()

    internal val inactiveVoipLibCall
        get() = if (isInTransfer) list.firstOrNull() else null

    internal val transferSession: AttendedTransferSession?
        get() = activeVoipLibCall?.let { active ->
            inactiveVoipLibCall?.let { inactive ->
                AttendedTransferSession(active, inactive)
            }
        }

    /**
     * The currently active call that is setup to send/receive audio.
     *
     */
    val active: Call?
        get() = factory.make(activeVoipLibCall)

    /**
     * The background call, this will only exist when there is a transfer
     * happening. This will be the initial call while connecting to the
     * new call.
     *
     */
    val inactive: Call?
        get() = factory.make(inactiveVoipLibCall)

    companion object {

        /**
         * The maximum number of calls that will be held in the call list.
         */
        const val MAX_CALLS = 2
    }
}