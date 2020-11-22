package nl.vialer.voip.android.audio

import nl.vialer.voip.android.CallManager
import nl.vialer.voip.android.VoIPPIL
import org.linphone.core.AudioDevice
import org.openvoipalliance.phonelib.PhoneLib

class AudioManager internal constructor(private val pil: VoIPPIL, private val phoneLib: PhoneLib, private val callManager: CallManager) {

    val isMicrophoneMuted: Boolean
        get() = phoneLib.microphoneMuted


    fun routeAudio(route: AudioRoute) {
//        callManager?.call?.linphoneCall?.outputAudioDevice = AudioDevice.
    }

    fun mute() {
        phoneLib.microphoneMuted = true
    }

    fun unmute() {
        phoneLib.microphoneMuted = false
    }

    fun toggleMute() {
        phoneLib.microphoneMuted = !phoneLib.microphoneMuted
    }
}