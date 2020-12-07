package org.openvoipalliance.androidplatformintegration.configuration

import android.app.Activity
import android.app.Application
import org.openvoipalliance.androidplatformintegration.logging.Logger
import org.openvoipalliance.androidplatformintegration.push.Middleware

data class ApplicationSetup(
    val application: Application,

    /**
     * References to activities that will be opened by notifications.
     *
     */
    val activities: Activities,

    /**
     * Provide a middleware if it is required to receive incoming calls
     * in your infrastructure.
     *
     */
    val middleware: Middleware? = null,

    /**
     * Receive logs from the PIL.
     *
     */
    val logger: Logger? = null,

    /**
     * If set to TRUE, we will start the designated call activity
     * when appropriate. If set to false the call activity will only be
     * opened when the user interacts with the call notification.
     *
     */
    val automaticallyStartCallActivity: Boolean = true,

    /**
     * The user-agent that will be used when making SIP calls.
     *
     */
    val userAgent: String = "AndroidPIL",
) {
    data class Activities(val call: Class<out Activity>?, val incomingCall: Class<out Activity>?)
}
