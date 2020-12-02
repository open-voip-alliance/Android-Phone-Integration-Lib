package nl.vialer.voip.android.configuration

import org.openvoipalliance.phonelib.model.Codec

data class Preferences(val codec: Array<Codec>, val usePhoneRingtone: Boolean) {
    companion object {
        val DEFAULT = Preferences(arrayOf(Codec.OPUS), false)
    }
}

