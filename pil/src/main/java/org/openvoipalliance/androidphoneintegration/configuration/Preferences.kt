package org.openvoipalliance.androidphoneintegration.configuration

import org.openvoipalliance.androidphoneintegration.contacts.SupplementaryContact
import org.openvoipalliance.voiplib.model.Codec

data class Preferences(
    val codecs: Array<Codec>,
    val useApplicationProvidedRingtone: Boolean,
    val supplementaryContacts: Set<SupplementaryContact>,
) {
    companion object {
        val DEFAULT = Preferences(arrayOf(Codec.OPUS), false, setOf())
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Preferences) return false

        return useApplicationProvidedRingtone == other.useApplicationProvidedRingtone
                && codecs contentEquals other.codecs
    }

    override fun hashCode(): Int {
        var result = codecs.contentHashCode()
        result = 31 * result + useApplicationProvidedRingtone.hashCode()
        return result
    }
}
