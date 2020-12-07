package org.openvoipalliance.androidplatformintegration.audio

import android.telecom.CallAudioState.*
import org.openvoipalliance.androidplatformintegration.CallManager
import org.openvoipalliance.androidplatformintegration.PIL
import org.openvoipalliance.phonelib.PhoneLib
import org.openvoipalliance.phonelib.model.Codec

class AudioManager internal constructor(
    private val pil: PIL,
    private val phoneLib: PhoneLib,
    private val callManager: CallManager
) {

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

        val default = AudioState(AudioRoute.PHONE, arrayOf(), null)

        val connection = pil.connection ?: return default

        if (connection?.callAudioState == null) {
            return default
        }

        val currentRoute = nativeRouteToPilRoute(connection.callAudioState.route)

        val routes = arrayOf(
            ROUTE_BLUETOOTH,
            ROUTE_EARPIECE,
            ROUTE_WIRED_HEADSET,
            ROUTE_WIRED_OR_EARPIECE,
            ROUTE_SPEAKER
        )

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

    private fun nativeRouteToPilRoute(nativeRoute: Int) = when (nativeRoute) {
        ROUTE_BLUETOOTH -> AudioRoute.BLUETOOTH
        ROUTE_EARPIECE, ROUTE_WIRED_HEADSET, ROUTE_WIRED_OR_EARPIECE -> AudioRoute.PHONE
        ROUTE_SPEAKER -> AudioRoute.SPEAKER
        else -> AudioRoute.PHONE
    }

    private fun pilRouteToNativeRoute(pilRoute: AudioRoute) = when (pilRoute) {
        AudioRoute.SPEAKER -> ROUTE_SPEAKER
        AudioRoute.PHONE -> ROUTE_WIRED_OR_EARPIECE
        AudioRoute.BLUETOOTH -> ROUTE_BLUETOOTH
    }
}
