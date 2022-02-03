package org.openvoipalliance.voiplib.repository.initialise

import org.openvoipalliance.voiplib.model.Call

interface CallListener {

    /**
     * An incoming call has been received by the library.
     *
     * You will likely want to alert the user to this via ringing, vibrating and/or displaying
     * an incoming call screen.
     */
    fun incomingCallReceived(call: Call) {}

    /**
     * The user of the application has started an outgoing call and it is has now
     * successfully been setup.
     *
     */
    fun outgoingCallCreated(call: Call) {}

    /**
     * The given call has been connected and the audio streams should be
     * working.
     *
     */
    fun callConnected(call: Call) {}

    /**
     * One party has ended the call, querying the reason on the call object
     * will provide more information about why the call has ended.
     *
     */
    fun callEnded(call: Call) {}

    fun callReleased(call: Call) {}

    /**
     * The state of the call has changed but it is likely not relevant, querying
     * the current state of the call can give you the exact change that occurred.
     *
     */
    fun callUpdated(call: Call) {}

    /**
     * An Attended Transfer has completed and the two calls have been merged, this will occur
     * before receiving the ended and released events.
     *
     */
    fun attendedTransferMerged(call: Call) {}

    /**
     * The first time the streams are started for a call.
     */
    fun streamsStarted() {}
}