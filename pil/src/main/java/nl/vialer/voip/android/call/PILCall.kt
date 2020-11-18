package nl.vialer.voip.android.call

data class PILCall(
    val remoteNumber: String,
    val displayName: String,
    val state: CallState,
    val direction: CallDirection,
    val duration: Int,
    val isOnHold: Boolean,
    val uuid: String,
    val mos: Float
) {
    val remotePartyHeading: String
        get() {
            return remoteNumber
        }

    val remotePartySubheading: String
        get() {
            return displayName
        }

    val prettyDuration: String
        get() {
            return duration.toString()
        }
}