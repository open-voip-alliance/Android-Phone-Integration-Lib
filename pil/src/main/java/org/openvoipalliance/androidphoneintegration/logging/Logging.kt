package org.openvoipalliance.androidphoneintegration.logging

fun interface Logger {
    fun onLogReceived(message: String, level: LogLevel)
}
