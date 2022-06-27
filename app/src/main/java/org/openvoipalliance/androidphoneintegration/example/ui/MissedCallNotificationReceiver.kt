package org.openvoipalliance.androidphoneintegration.example.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class MissedCallNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Toast.makeText(context, "Missed call notification pressed", Toast.LENGTH_LONG).show()
    }

    enum class Action {
        MISSED_CALL_NOTIFICATION_PRESSED
    }
}
