package org.openvoipalliance.androidphoneintegration.audio

data class AudioState(
    val currentRoute: AudioRoute,
    val availableRoutes: Array<AudioRoute>,
    val bluetoothDeviceName: String?,
    val isMicrophoneMuted: Boolean,
)
