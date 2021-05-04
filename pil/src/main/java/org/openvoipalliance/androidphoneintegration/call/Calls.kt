package org.openvoipalliance.androidphoneintegration.call


class Calls internal constructor(private val callManager: CallManager, private val factory: CallFactory) {

    /**
     * The currently active call that is setup to send/receive audio.
     *
     */
    val active: Call?
        get() = factory.make(findActiveCall())

    /**
     * The background call, this will only exist when there is a transfer
     * happening. This will be the initial call while connecting to the
     * new call.
     *
     */
    val inactive: Call?
        get() = factory.make(findInactiveCall())

    val isInTransfer: Boolean
        get() = callManager.transferSession != null

    private fun findActiveCall(): VoipLibCall? {
        callManager.transferSession?.let {
            return it.to
        }

        return callManager.voipLibCall
    }

    private fun findInactiveCall(): VoipLibCall? {
        callManager.transferSession?.let { return it.from }

        return null
    }
}