package nl.vialer.voip.android.configuration

import org.openvoipalliance.phonelib.model.Codec

data class Preferences(val codecs: Array<Codec>, val useApplicationProvidedRingtone: Boolean) {
    companion object {
        val DEFAULT = Preferences(arrayOf(Codec.OPUS), false)
    }
}
