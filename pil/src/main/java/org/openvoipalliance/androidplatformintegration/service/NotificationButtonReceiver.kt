package org.openvoipalliance.androidplatformintegration.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.openvoipalliance.androidplatformintegration.PIL
import org.openvoipalliance.androidplatformintegration.service.NotificationButtonReceiver.Action.*

internal class NotificationButtonReceiver : BroadcastReceiver() {

    private val pil by lazy { PIL.instance }

    override fun onReceive(context: Context, intent: Intent) {
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
