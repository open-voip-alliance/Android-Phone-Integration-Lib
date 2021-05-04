package org.openvoipalliance.androidphoneintegration

import org.openvoipalliance.androidphoneintegration.audio.AudioState
import org.openvoipalliance.androidphoneintegration.call.Call

data class CallSessionState(
    val activeCall: Call?,
    val inactiveCall: Call?,
    val audioState: AudioState
)