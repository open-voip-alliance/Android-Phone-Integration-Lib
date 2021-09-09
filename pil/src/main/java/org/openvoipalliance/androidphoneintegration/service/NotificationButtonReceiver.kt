package org.openvoipalliance.androidphoneintegration.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.openvoipalliance.androidphoneintegration.PIL
import org.openvoipalliance.androidphoneintegration.helpers.stopVoipService
import org.openvoipalliance.androidphoneintegration.notifications.IncomingCallNotification
import org.openvoipalliance.androidphoneintegration.service.NotificationButtonReceiver.Action.*

internal class NotificationButtonReceiver : BroadcastReceiver() {

    private val pil by lazy { PIL.instance }
    private val incomingCallNotification by lazy { IncomingCallNotification() }

    override fun onReceive(context: Context, intent: Intent) {
        // If there is no PIL, we should no longer be responding to the notifications being
        // pressed.
        if (!PIL.isInitialized) {
            incomingCallNotification.cancel()
            context.stopVoipService()
            return
        }

        // This exists to resolve a problem where the incoming call notification would remain
        // visible even when there is no call. A solution should be found to fix the underlying
        // issue but currently this at least provides a way out for users so their phone
        // stops ringing.
        if (!pil.calls.isInCall) {
            incomingCallNotification.cancel()
            return
        }

        try {
            val action = valueOf(intent.action ?: "")

            pil.writeLog("Received notification button press with action: $action")

            when (action) {
                HANG_UP -> pil.actions.end()
                ANSWER -> pil.actions.answer()
                DECLINE -> pil.actions.decline()
                MISSED_CALL_NOTIFICATION_PRESSED -> pil.app.onMissedCallNotificationPressed?.invoke()
            }
        } catch (e: IllegalArgumentException) {
            pil.writeLog(
                "Unable to handle broadcast sent to NotificationButtonReceiver: ${intent.action}"
            )
            return
        }
    }

    enum class Action {
        HANG_UP, ANSWER, DECLINE, MISSED_CALL_NOTIFICATION_PRESSED
    }
}
