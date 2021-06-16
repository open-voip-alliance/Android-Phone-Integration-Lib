package org.openvoipalliance.androidphoneintegration.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MIN
import org.openvoipalliance.androidphoneintegration.R
import org.openvoipalliance.androidphoneintegration.call.Call

internal class MissedCallNotification: Notification() {
    override val channelId = CHANNEL_ID

    override val notificationId = NOTIFICATION_ID

    override fun createNotificationChannel() {
        notificationManger.createNotificationChannel(NotificationChannel(
            channelId,
            context.getString(R.string.notification_incoming_calls_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ))
    }

    fun notify(call: Call) {
        createNotificationChannel()

        val notification = NotificationCompat.Builder(pil.app.application, channelId).apply {
            setOngoing(false)
            setSmallIcon(R.drawable.ic_missed_calls_notification_icon)
            setContentTitle(context.getString(R.string.notification_missed_calls_title))
            setContentText(call.remotePartyHeading)
            priority = PRIORITY_MIN
        }.build()

        notificationManger.notify(notificationId, notification)
    }

    companion object {
        private const val NOTIFICATION_ID = 781
        private const val CHANNEL_ID = "MissedCalls"
    }
}