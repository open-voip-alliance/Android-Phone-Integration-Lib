package org.openvoipalliance.voiplib.config

import org.openvoipalliance.voiplib.model.Codec
import org.openvoipalliance.voiplib.model.Codec.*
import org.openvoipalliance.voiplib.repository.initialise.CallListener
import org.openvoipalliance.voiplib.repository.initialise.LogListener

typealias GlobalStateCallback = () -> Unit

data class Config(
        val callListener: CallListener = object : CallListener {},
        val stun: String? = null,
        val ring: String? = null,
        val logListener: LogListener? = null,
        val codecs: Array<Codec> = arrayOf(
                G722,
                G729,
                GSM,
                ILBC,
                ISAC,
                L16,
                OPUS,
                PCMA,
                PCMU,
                SPEEX,
        ),
        val userAgent: String = "AndroidVoIPLib",
        val onReady: GlobalStateCallback = {},
        val onDestroy: GlobalStateCallback = {},
)