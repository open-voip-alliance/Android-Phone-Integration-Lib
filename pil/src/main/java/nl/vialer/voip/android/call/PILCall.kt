package nl.vialer.voip.android.call

data class PILCall(
    val state: CallState,
    val direction: CallDirection,
    val duration: Int,
    val isOnHold: Boolean,
    val uuid: String,
    val mos: Float
) {
    val remotePartyHeading: String
        get() {
            return ""
        }

    val remotePartySubheading: String
        get() {
            return ""
        }

    val prettyDuration: String
        get() {
            return duration.toString()
        }
}