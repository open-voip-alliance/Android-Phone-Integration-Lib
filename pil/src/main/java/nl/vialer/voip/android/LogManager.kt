package nl.vialer.voip.android

import android.util.Log
import org.openvoipalliance.phonelib.repository.initialise.LogLevel
import org.openvoipalliance.phonelib.repository.initialise.LogListener

internal class LogManager(private val pil: VoIPPIL): LogListener {

    override fun onLogMessageWritten(lev: LogLevel, message: String) {
        pil.logger.invoke(when(lev) {
            LogLevel.DEBUG -> nl.vialer.voip.android.logging.LogLevel.DEBUG
            LogLevel.TRACE -> nl.vialer.voip.android.logging.LogLevel.DEBUG
            LogLevel.MESSAGE -> nl.vialer.voip.android.logging.LogLevel.INFO
            LogLevel.WARNING -> nl.vialer.voip.android.logging.LogLevel.WARNING
            LogLevel.ERROR -> nl.vialer.voip.android.logging.LogLevel.ERROR
            LogLevel.FATAL -> nl.vialer.voip.android.logging.LogLevel.ERROR
        }, message)
    }

}