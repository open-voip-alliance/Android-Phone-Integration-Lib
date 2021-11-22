package org.openvoipalliance.voiplib.repository.initialise


interface LogListener {
    fun onLogMessageWritten(lev: LogLevel, message: String)
}

enum class LogLevel {
    DEBUG, TRACE, MESSAGE, WARNING, ERROR, FATAL
}