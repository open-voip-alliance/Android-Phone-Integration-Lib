package org.openvoipalliance.androidplatformintegration.events

import org.openvoipalliance.androidplatformintegration.call.PILCall

interface PILEventListener {
    fun onEvent(event: Event)
}

sealed class Event {

    sealed class CallEvent(val call: PILCall?): Event() {
        class OutgoingCallStarted(call: PILCall?) : CallEvent(call)
        class IncomingCallReceived(call: PILCall?) : CallEvent(call)
        class CallEnded(call: PILCall?) : CallEvent(call)
        class CallUpdated(call: PILCall?) : CallEvent(call)
        class CallConnected(call: PILCall?) : CallEvent(call)
    }
}
