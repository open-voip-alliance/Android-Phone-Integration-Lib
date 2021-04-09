package org.openvoipalliance.androidphoneintegration.logging

import org.openvoipalliance.androidphoneintegration.PIL

internal class LogManager(private val pil: PIL) {

    fun writeLog(message: String, level: LogLevel = LogLevel.INFO) {
        pil.app.logger?.onLogReceived(message, level)
    }

    fun logVersion() {
        writeLog(pil.versionInfo.toString())
    }
}