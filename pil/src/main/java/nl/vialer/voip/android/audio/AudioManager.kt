package nl.vialer.voip.android.audio

import android.telecom.CallAudioState.*
import nl.vialer.voip.android.CallManager
import nl.vialer.voip.android.PIL
import nl.vialer.voip.android.audio.AudioRoute.*
import org.openvoipalliance.phonelib.PhoneLib
import org.openvoipalliance.phonelib.model.Codec

class AudioManager internal constructor(private val pil: PIL, private val phoneLib: PhoneLib, private val callManager: CallManager) {

    val availableCodecs = arrayOf(Codec.OPUS, Codec.ILBC, Codec.G729, Codec.SPEEX)

    val isMicrophoneMuted: Boolean
        get() = phoneLib.microphoneMuted

    val state: AudioState
        get() = createAudioStateObject()

    fun routeAudio(route: AudioRoute) {
        pil.connection?.setAudioRoute(pilRouteToNativeRoute(route))
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

    private fun createAudioStateObject(): AudioState {

        val default = AudioState(PHONE, arrayOf(), null)

        val connection = pil.connection ?: return default

        if (connection?.callAudioState == null) {
            return default
        }

        val currentRoute = nativeRouteToPilRoute(connection.callAudioState.route)

        val routes = arrayOf(ROUTE_BLUETOOTH, ROUTE_EARPIECE, ROUTE_WIRED_HEADSET, ROUTE_WIRED_OR_EARPIECE, ROUTE_SPEAKER)

        val supportedRoutes = mutableListOf<AudioRoute>()

        routes.forEach {
            if (connection.callAudioState.supportedRouteMask and it == it) {
                supportedRoutes.add(nativeRouteToPilRoute(it))
            }
        }

        val bluetoothName = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            connection.callAudioState.activeBluetoothDevice?.name
        } else {
            null
        }

        return AudioState(currentRoute, supportedRoutes.toTypedArray(), bluetoothName)
    }

    private fun nativeRouteToPilRoute(nativeRoute: Int) = when(nativeRoute) {
        ROUTE_BLUETOOTH -> BLUETOOTH
        ROUTE_EARPIECE, ROUTE_WIRED_HEADSET, ROUTE_WIRED_OR_EARPIECE -> PHONE
        ROUTE_SPEAKER -> SPEAKER
        else -> PHONE
    }

    private fun pilRouteToNativeRoute(pilRoute: AudioRoute) = when(pilRoute) {
        SPEAKER -> ROUTE_SPEAKER
        PHONE -> ROUTE_WIRED_OR_EARPIECE
        BLUETOOTH -> ROUTE_BLUETOOTH
    }
}