package org.openvoipalliance.androidphoneintegration.call

import org.openvoipalliance.voiplib.model.Call

class Calls internal constructor(private val callManager: CallManager, private val factory: PILCallFactory) {

    /**
     * The currently active call that is setup to send/receive audio.
     *
     */
    val active: PILCall?
        get() = factory.make(findActiveCall())

    /**
     * The background call, this will only exist when there is a transfer
     * happening. This will be the initial call while connecting to the
     * new call.
     *
     */
    val inactive: PILCall?
        get() = factory.make(findInactiveCall())

    val isInTransfer: Boolean
        get() = callManager.transferSession != null

    private fun findActiveCall(): Call? {
        callManager.transferSession?.let {
            return it.to
        }

        return callManager.call
    }

    private fun findInactiveCall(): Call? {
        callManager.transferSession?.let { return it.from }

        return null
    }
}