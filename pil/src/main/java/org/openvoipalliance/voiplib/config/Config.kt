package org.openvoipalliance.voiplib.config

import org.openvoipalliance.voiplib.model.Codec
import org.openvoipalliance.voiplib.repository.initialise.CallListener
import org.openvoipalliance.voiplib.repository.initialise.LogListener

typealias GlobalStateCallback = () -> Unit

data class Config(
        val auth: Auth,
        val callListener: CallListener = object : CallListener {},
        val stun: String? = null,
        val ring: String? = null,
        val logListener: LogListener? = null,
        val codecs: Array<Codec> = arrayOf(Codec.G722, Codec.G729, Codec.GSM, Codec.ILBC, Codec.ISAC, Codec.L16, Codec.OPUS, Codec.PCMA, Codec.PCMU, Codec.SPEEX),
        val userAgent: String = "AndroidVoIPLib",
        val onReady: GlobalStateCallback = {},
        val onDestroy: GlobalStateCallback = {},
        val advancedVoIPSettings: AdvancedVoIPSettings = AdvancedVoIPSettings()
)