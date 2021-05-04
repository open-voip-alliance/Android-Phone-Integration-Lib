package org.openvoipalliance.androidphoneintegration.audio

import android.telecom.CallAudioState.*
import org.openvoipalliance.androidphoneintegration.events.Event
import org.openvoipalliance.androidphoneintegration.events.EventsManager
import org.openvoipalliance.androidphoneintegration.telecom.AndroidCallFramework
import org.openvoipalliance.voiplib.VoIPLib
import org.openvoipalliance.voiplib.model.Codec

class AudioManager internal constructor(
    private val phoneLib: VoIPLib,
    private val androidCallFramework: AndroidCallFramework,
    private val events: EventsManager
) {

    val availableCodecs = arrayOf(Codec.OPUS, Codec.ILBC, Codec.G729, Codec.SPEEX)

    var isMicrophoneMuted: Boolean
        get() = phoneLib.microphoneMuted
        private set(value) {
            phoneLib.microphoneMuted = value
            events.broadcast(Event.CallSessionEvent.AudioStateUpdated::class)
        }

    val state: AudioState
        get() = createAudioStateObject()

    fun routeAudio(route: AudioRoute) =
        androidCallFramework.connection?.setAudioRoute(pilRouteToNativeRoute(route))

    fun mute() {
        isMicrophoneMuted = true
    }

    fun unmute() {
        isMicrophoneMuted = false
    }

    fun toggleMute() {
        isMicrophoneMuted = !isMicrophoneMuted
    }

    private fun createAudioStateObject(): AudioState {

        val default = AudioState(AudioRoute.PHONE, arrayOf(), null, false)

        val connection = androidCallFramework.connection ?: return default

        if (connection.callAudioState == null) {
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

        return AudioState(currentRoute, supportedRoutes.toTypedArray(), bluetoothName, phoneLib.microphoneMuted)
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
