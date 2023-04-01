package org.openvoipalliance.androidphoneintegration.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.openvoipalliance.androidphoneintegration.PIL
import org.openvoipalliance.androidphoneintegration.service.NotificationButtonReceiver.Action.*


internal class NotificationButtonReceiver : BroadcastReceiver() {

    private val pil by lazy { PIL.instance }

    override fun onReceive(context: Context, intent: Intent) {
        // If there is no PIL, we should no longer be responding to the notifications being
        // pressed.
        if (!PIL.isInitialized) {
            return
        }

        pil.notifications.dismissStale()

        // We need to start the VoIP service here, or we don't have access to the microphone.
        // See: https://developer.android.com/guide/components/foreground-services#bg-access-restriction-exemptions
        context.startService(Intent(context, VoIPService::class.java))

        try {
            val action = valueOf(intent.action ?: "")

            pil.writeLog("Received notification button press with action: $action")

            when (action) {
                HANG_UP -> pil.actions.end()
                ANSWER -> pil.actions.answer()
                DECLINE -> pil.actions.decline()
            }
        } catch (e: IllegalArgumentException) {
            pil.writeLog(
                "Unable to handle broadcast sent to NotificationButtonReceiver: ${intent.action}"
            )
            return
        }
    }

    enum class Action {
        HANG_UP, ANSWER, DECLINE
    }
}
