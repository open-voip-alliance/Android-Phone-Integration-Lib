package org.openvoipalliance.androidphoneintegration.configuration

import org.openvoipalliance.androidphoneintegration.contacts.SupplementaryContact

data class Preferences(
    val useApplicationProvidedRingtone: Boolean,
    val supplementaryContacts: Set<SupplementaryContact> = setOf(),
) {
    companion object {
        val DEFAULT = Preferences(useApplicationProvidedRingtone = false)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Preferences) return false

        return useApplicationProvidedRingtone == other.useApplicationProvidedRingtone
                && supplementaryContacts == other.supplementaryContacts
    }

    override fun hashCode(): Int {
        var result = useApplicationProvidedRingtone.hashCode()
        result = 31 * result + supplementaryContacts.hashCode()
        return result
    }
}
