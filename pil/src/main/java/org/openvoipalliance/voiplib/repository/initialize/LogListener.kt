package org.openvoipalliance.voiplib.repository.initialize


interface LogListener {
    fun onLogMessageWritten(lev: LogLevel, message: String)
}

enum class LogLevel {
    DEBUG, TRACE, MESSAGE, WARNING, ERROR, FATAL
}