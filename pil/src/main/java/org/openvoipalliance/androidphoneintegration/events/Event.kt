package org.openvoipalliance.androidphoneintegration.events

import org.openvoipalliance.androidphoneintegration.call.PILCall

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

    sealed class CallSetupFailedEvent(val reason: Reason): Event() {
        class OutgoingCallSetupFailed(reason: Reason): CallSetupFailedEvent(reason)
        class IncomingCallSetupFailed(reason: Reason): CallSetupFailedEvent(reason)

        enum class Reason {
            REJECTED_BY_ANDROID_TELECOM_FRAMEWORK,
            UNABLE_TO_REGISTER
        }
    }
}