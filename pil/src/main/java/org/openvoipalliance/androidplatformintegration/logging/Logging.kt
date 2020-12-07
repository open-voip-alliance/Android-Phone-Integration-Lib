package org.openvoipalliance.androidplatformintegration.logging

fun interface Logger {
    fun onLogReceived(message: String, level: LogLevel)
}
