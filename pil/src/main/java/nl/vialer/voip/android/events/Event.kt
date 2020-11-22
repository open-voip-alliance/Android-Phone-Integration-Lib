package nl.vialer.voip.android.events

interface EventListener {
    fun onEvent(event: Event)
}

enum class Event {
    OUTGOING_CALL_STARTED, INCOMING_CALL_RECEIVED, CALL_ENDED, CALL_UPDATED
}