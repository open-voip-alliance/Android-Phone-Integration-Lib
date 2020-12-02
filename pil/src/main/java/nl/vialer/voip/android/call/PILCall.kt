package nl.vialer.voip.android.call

import android.content.Context
import android.text.format.DateUtils
import nl.vialer.voip.android.CallManager
import nl.vialer.voip.android.contacts.Contact
import nl.vialer.voip.android.contacts.Contacts
import org.openvoipalliance.phonelib.model.Call
import org.openvoipalliance.phonelib.model.Direction
import java.util.*

data class PILCall(
    val remoteNumber: String,
    val displayName: String,
    val state: CallState,
    val direction: CallDirection,
    val duration: Int,
    val isOnHold: Boolean,
    val uuid: String,
    val mos: Float,
    val contact: Contact?
) {
    val remotePartyHeading: String
        get() {
            if (contact != null) {
                return contact.name
            }

            if (displayName.isNotBlank()) {
                return displayName
            }

            return remoteNumber
        }

    val remotePartySubheading: String
        get() {
            if (contact != null || displayName.isNotBlank()) {
                return remoteNumber
            }

            return ""
        }

    val prettyDuration: String
        get() {
            return DateUtils.formatElapsedTime(duration.toLong())
        }
}