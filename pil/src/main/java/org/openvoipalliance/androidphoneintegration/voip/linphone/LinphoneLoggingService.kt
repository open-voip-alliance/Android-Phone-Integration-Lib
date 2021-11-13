package org.openvoipalliance.androidphoneintegration.voip.linphone

import org.linphone.core.LogLevel
import org.linphone.core.LogLevel.*
import org.linphone.core.LoggingService
import org.linphone.core.LoggingServiceListener
import org.openvoipalliance.androidphoneintegration.PIL
import org.openvoipalliance.androidphoneintegration.log

internal class LinphoneLoggingService(private val pil: PIL) : LoggingServiceListener {
    override fun onLogMessageWritten(
        logService: LoggingService,
        domain: String,
        level: LogLevel,
        message: String,
    ) = log(message, when(level) {
            Debug -> org.openvoipalliance.androidphoneintegration.logging.LogLevel.DEBUG
            Trace -> org.openvoipalliance.androidphoneintegration.logging.LogLevel.DEBUG
            Message -> org.openvoipalliance.androidphoneintegration.logging.LogLevel.INFO
            Warning -> org.openvoipalliance.androidphoneintegration.logging.LogLevel.WARNING
            Error -> org.openvoipalliance.androidphoneintegration.logging.LogLevel.ERROR
            Fatal -> org.openvoipalliance.androidphoneintegration.logging.LogLevel.ERROR
        })
}