package nl.vialer.voip.android.events

typealias EventListener = (event: Event) -> Unit

enum class Event {
    OUTGOING_CALL_STARTED, CALL_ENDED
}