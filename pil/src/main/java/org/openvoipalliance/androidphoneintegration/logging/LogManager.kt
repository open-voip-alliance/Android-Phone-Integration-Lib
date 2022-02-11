package org.openvoipalliance.androidphoneintegration.logging

import org.openvoipalliance.androidphoneintegration.PIL
import org.openvoipalliance.androidphoneintegration.logging.LogLevel.*
import org.openvoipalliance.voiplib.repository.initialize.LogListener
import org.openvoipalliance.voiplib.repository.initialize.LogLevel as VoipLibLogLevel

internal class LogManager(private val pil: PIL) : LogListener {

    fun writeLog(message: String, level: LogLevel = INFO) {
        pil.app.logger?.onLogReceived(message, level)
    }

    fun logVersion() {
        writeLog(pil.versionInfo.toString())
    }

    /**
     * Receiving the logs from the phone lib.
     *
     * @param lev
     * @param message
     */
    override fun onLogMessageWritten(
        lev: VoipLibLogLevel,
        message: String,
    ) = writeLog(
            message,
            when (lev) {
                VoipLibLogLevel.DEBUG -> DEBUG
                VoipLibLogLevel.TRACE -> DEBUG
                VoipLibLogLevel.MESSAGE -> INFO
                VoipLibLogLevel.WARNING -> WARNING
                VoipLibLogLevel.ERROR -> ERROR
                VoipLibLogLevel.FATAL -> ERROR
            }
        )
}