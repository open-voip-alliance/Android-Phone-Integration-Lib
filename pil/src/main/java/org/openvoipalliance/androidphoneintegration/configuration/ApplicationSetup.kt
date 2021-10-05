package org.openvoipalliance.androidphoneintegration.configuration

import android.app.Activity
import android.app.Application
import org.openvoipalliance.androidphoneintegration.R
import org.openvoipalliance.androidphoneintegration.logging.Logger
import org.openvoipalliance.androidphoneintegration.push.Middleware

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
    val automaticallyLaunchCallActivity: AutomaticallyLaunchCallActivity = AutomaticallyLaunchCallActivity.ALWAYS,

    /**
     * The user-agent that will be used when making SIP calls.
     *
     */
    val userAgent: String = "AndroidPIL",

    /**
     * Show a notification to the user when a call is not answered.
     *
     * This notification can be customised by providing different resources:
     * @see R.string.notification_missed_calls_channel_name
     * @see R.drawable.ic_missed_calls_notification_icon
     * @see R.plurals.notification_missed_call_title
     * @see R.plurals.notification_missed_call_subtitle
     */
    val notifyOnMissedCall: Boolean = true,

    /**
     * A callback that will be delivered to the app when the user presses a missed call
     * notification.
     *
     */
    val onMissedCallNotificationPressed: (() -> Unit)? = null
) {
    enum class AutomaticallyLaunchCallActivity {
        // Activities will never be automatically launched (they will still be launched via direct user input)
        NEVER,

        // Activities will be launched if the app is in the background, such as on call connect
        ONLY_FROM_BACKGROUND,

        // We will always launch activities, this means the consuming app does not need to manually transition between activities
        ALWAYS
    }

    data class Activities(val call: Class<out Activity>?, val incomingCall: Class<out Activity>?)
}
