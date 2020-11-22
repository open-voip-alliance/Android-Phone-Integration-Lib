package nl.vialer.voip.android.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import nl.vialer.voip.android.VoIPPIL
import nl.vialer.voip.android.service.NotificationButtonReceiver.Action.*

class NotificationButtonReceiver: BroadcastReceiver() {

    private val pil by lazy { VoIPPIL.instance }

    override fun onReceive(context: Context, intent: Intent) {
        val actionString = intent.getStringExtra(CALL_ACTION_EXTRA) ?: return

        val action = valueOf(actionString)

        when (action) {
            HANG_UP -> pil.endCall()
        }
    }

    companion object {
        const val CALL_ACTION_EXTRA = "CALL_ACTION_EXTRA"
    }

    enum class Action {
        HANG_UP
    }
}