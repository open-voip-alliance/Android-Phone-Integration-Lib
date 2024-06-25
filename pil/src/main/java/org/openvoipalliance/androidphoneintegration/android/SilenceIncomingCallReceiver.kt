package org.openvoipalliance.androidphoneintegration.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import org.openvoipalliance.androidphoneintegration.PIL
import kotlin.math.PI

class SilenceIncomingCallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        PIL.instance.calls.active?.let {
            PIL.instance.notifications.incomingCallNotification.cancel()
        }
    }
}