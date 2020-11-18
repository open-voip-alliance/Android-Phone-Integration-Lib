package nl.vialer.voip.android.audio

data class AudioState(
    val currentRoute: AudioRoute,
    val availableRoutes: Array<AudioRoute>,
    val bluetoothDeviceName: String?
)