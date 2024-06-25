package org.openvoipalliance.androidphoneintegration.notifications

import org.openvoipalliance.androidphoneintegration.PIL
import org.openvoipalliance.androidphoneintegration.log

internal class NotificationManager(
    private val pil: PIL,
    public val incomingCallNotification: IncomingCallNotification,
) {

    /**
     * Dismiss any notifications that should no longer be active. This can be called liberally
     * as it will check the state before taking any action.
     *
     */
    fun dismissStale() {
        if (!pil.calls.isInCall) {
            log("Dismissing stale incoming call notification")

            incomingCallNotification.cancel()
        }
    }
}