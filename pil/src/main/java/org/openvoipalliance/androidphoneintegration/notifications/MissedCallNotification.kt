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

    private val hasActiveNotification
        get() = notificationManger.activeNotifications.any { it.id == NOTIFICATION_ID }

    override fun createNotificationChannel() {
        notificationManger.createNotificationChannel(NotificationChannel(
            channelId,
            context.getString(R.string.notification_incoming_calls_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ))
    }

    fun notify(call: Call) {
        createNotificationChannel()

        if (hasActiveNotification) {
            count++
        } else {
            count = 1
        }

        val title = context.resources.getQuantityString(
            R.plurals.notification_missed_call_title,
            count,
        )

        val subtitle = context.resources.getQuantityString(
            R.plurals.notification_missed_call_subtitle,
            count,
            if (hasActiveNotification) count else call.remotePartyHeading,
        )

        val notification = NotificationCompat.Builder(pil.app.application, channelId).apply {
            setOngoing(false)
            setSmallIcon(R.drawable.ic_missed_calls_notification_icon)
            setContentTitle(title)
            setContentText(subtitle)
            setAutoCancel(true)
            setCategory(android.app.Notification.CATEGORY_STATUS)
            priority = PRIORITY_MIN
            setContentIntent(pil.app.onMissedCallNotificationPressed)
        }.build()

        notificationManger.notify(notificationId, notification)
    }

    companion object {
        private const val NOTIFICATION_ID = 781
        private const val CHANNEL_ID = "MissedCalls"

        private var count = 1
    }
}