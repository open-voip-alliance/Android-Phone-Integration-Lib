package nl.vialer.voip.android.audio

data class AudioState(
    val currentRoute: AudioRoute,
    val hasBluetoothAudioRouteAvailable: Boolean,
    val bluetoothDeviceName: String?
)