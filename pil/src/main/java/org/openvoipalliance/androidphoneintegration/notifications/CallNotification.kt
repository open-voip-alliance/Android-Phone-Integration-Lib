package org.openvoipalliance.androidphoneintegration.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import org.openvoipalliance.androidphoneintegration.R
import org.openvoipalliance.androidphoneintegration.call.PILCall
import org.openvoipalliance.androidphoneintegration.service.NotificationButtonReceiver
import org.openvoipalliance.androidphoneintegration.service.VoIPService

internal class CallNotification: Notification() {

    override val channelId = CHANNEL_ID

    public override val notificationId = NOTIFICATION_ID

    override fun createNotificationChannel() {
        notificationManger.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                setShowBadge(false)
            }
        )
    }

    internal fun update(call: PILCall) {
        notificationManger.notify(
            notificationId,
            build()
                .setContentTitle(call.remotePartyHeading)
                .setContentText(call.prettyDuration)
                .build()
        )
    }

    fun build(): NotificationCompat.Builder {
        createNotificationChannel()

        return NotificationCompat.Builder(context, channelId)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentTitle(context.getString(R.string.notification_default_title))
            .setContentText(context.getString(R.string.notification_default_subtitle))
            .setSmallIcon(R.drawable.ic_service)

            .setShowWhen(false)
            .setColorized(true)
            .setOngoing(true)
            .setAutoCancel(false)
            .setColor(context.getColor(R.color.notification_background))
            .addAction(
                R.drawable.ic_service,
                context.getString(R.string.notification_hang_up_action),
                createActionIntent(NotificationButtonReceiver.Action.HANG_UP, pil.app.application)
            )
            .setContentIntent(PendingIntent.getActivity(
                context,
                0,
                Intent(context, pil.app.activities.call),
                0
            ))
    }

    companion object {
        private const val NOTIFICATION_ID = 341
        private const val CHANNEL_ID = "VoIP"
    }
}