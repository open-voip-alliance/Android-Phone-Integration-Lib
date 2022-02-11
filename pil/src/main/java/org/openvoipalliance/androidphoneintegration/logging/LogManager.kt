package org.openvoipalliance.androidphoneintegration.logging

import org.openvoipalliance.androidphoneintegration.PIL
import org.openvoipalliance.androidphoneintegration.logging.LogLevel.*
import org.openvoipalliance.voiplib.repository.initialise.LogListener

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
        lev: org.openvoipalliance.voiplib.repository.initialise.LogLevel,
        message: String,
    ) = writeLog(
            message,
            when (lev) {
                org.openvoipalliance.voiplib.repository.initialise.LogLevel.DEBUG -> DEBUG
                org.openvoipalliance.voiplib.repository.initialise.LogLevel.TRACE -> DEBUG
                org.openvoipalliance.voiplib.repository.initialise.LogLevel.MESSAGE -> INFO
                org.openvoipalliance.voiplib.repository.initialise.LogLevel.WARNING -> WARNING
                org.openvoipalliance.voiplib.repository.initialise.LogLevel.ERROR -> ERROR
                org.openvoipalliance.voiplib.repository.initialise.LogLevel.FATAL -> ERROR
            }
        )
}