package nl.vialer.voip.android.call

enum class CallState {
    INITIALIZING, RINGING, CONNECTED, HELD_BY_LOCAL, HELD_BY_REMOTE, ENDED, ERROR
}