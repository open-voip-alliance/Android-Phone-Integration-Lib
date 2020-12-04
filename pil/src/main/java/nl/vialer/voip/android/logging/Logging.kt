package nl.vialer.voip.android.logging

fun interface Logger {
    fun onLogReceived(message: String, level: LogLevel)
}
