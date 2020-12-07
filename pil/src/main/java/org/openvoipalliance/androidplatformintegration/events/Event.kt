package org.openvoipalliance.androidplatformintegration.events

interface PILEventListener {
    fun onEvent(event: Event)
}

enum class Event {
    OUTGOING_CALL_STARTED, INCOMING_CALL_RECEIVED, CALL_ENDED, CALL_UPDATED, CALL_CONNECTED
}
