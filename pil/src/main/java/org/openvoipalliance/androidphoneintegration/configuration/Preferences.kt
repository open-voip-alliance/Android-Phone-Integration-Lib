package org.openvoipalliance.androidphoneintegration.configuration

import org.openvoipalliance.voiplib.model.Codec

data class Preferences(val codecs: Array<Codec>, val useApplicationProvidedRingtone: Boolean) {
    companion object {
        val DEFAULT = Preferences(arrayOf(Codec.OPUS), false)
    }
}
