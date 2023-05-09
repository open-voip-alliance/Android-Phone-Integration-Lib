package org.openvoipalliance.androidphoneintegration.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Person
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import org.openvoipalliance.androidphoneintegration.R
import org.openvoipalliance.androidphoneintegration.call.Call
import org.openvoipalliance.androidphoneintegration.service.NotificationButtonReceiver

internal class CallNotification : Notification() {

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

    internal fun update(call: Call) {
        notificationManger.notify(notificationId, build(call))
    }

    fun build(call: Call? = null): android.app.Notification =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && call != null) {
            android.app.Notification.Builder(context, channelId).apply {
                setSmallIcon(R.drawable.ic_service)
                setWhen(System.currentTimeMillis() - (call.duration * 1000))
                setShowWhen(true)
                setOngoing(true)
                setCategory(android.app.Notification.CATEGORY_CALL)
                setContentIntent(PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, pil.app.activities.call),
                    PendingIntent.FLAG_IMMUTABLE
                ))
                setUsesChronometer(true)
                style = android.app.Notification.CallStyle.forOngoingCall(
                    call.toPerson(),
                    createActionIntent(NotificationButtonReceiver.Action.HANG_UP,
                        pil.app.application),
                )
            }.build()
        } else {
            NotificationCompat.Builder(context, channelId)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentTitle(call?.remotePartyHeading
                    ?: context.getString(R.string.notification_default_title))
                .setContentText(call?.prettyDuration
                    ?: context.getString(R.string.notification_default_subtitle))
                .setSmallIcon(R.drawable.ic_service)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true)
                .setColorized(true)
                .setOngoing(true)
                .setAutoCancel(false)
                .setCategory(android.app.Notification.CATEGORY_SERVICE)
                .setColor(context.getColor(R.color.notification_background))
                .addAction(
                    R.drawable.ic_service,
                    context.getString(R.string.notification_hang_up_action),
                    createActionIntent(NotificationButtonReceiver.Action.HANG_UP,
                        pil.app.application)
                )
                .setContentIntent(PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, pil.app.activities.call),
                    PendingIntent.FLAG_IMMUTABLE
                )).build()
        }

    companion object {
        private const val NOTIFICATION_ID = 341
        private const val CHANNEL_ID = "VoIP"
    }
}

@RequiresApi(Build.VERSION_CODES.P)
fun Call.toPerson(): Person = Person.Builder().apply {
        setName(prettyRemoteParty)
        contact?.let { contact ->
            contact.imageUri?.let { uri ->
                setIcon(Icon.createWithContentUri(uri))
            }
        }
        setImportant(true)
    }.build()