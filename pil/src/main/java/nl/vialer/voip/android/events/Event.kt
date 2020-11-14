package nl.vialer.voip.android.events

typealias EventListener = (event: Event) -> Unit

enum class Event {
    CALL_DID_BEGIN, CALL_DID_END, CALL_STATE_CHANGED
}