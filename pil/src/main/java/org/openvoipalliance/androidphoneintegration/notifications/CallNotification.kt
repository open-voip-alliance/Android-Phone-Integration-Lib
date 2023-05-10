package org.openvoipalliance.androidphoneintegration.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Person
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import org.openvoipalliance.androidphoneintegration.PIL
import org.openvoipalliance.androidphoneintegration.R
import org.openvoipalliance.androidphoneintegration.call.Call
import org.openvoipalliance.androidphoneintegration.service.NotificationButtonReceiver

internal class CallNotification : Notification() {

    override val channelId = CHANNEL_ID
    override val notificationId = 4567

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

    fun build(call: Call? = null): android.app.Notification = createNotificationChannel().run {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && call != null) {
            buildCallStyleNotification(call)
        } else {
            buildLegacyNotification(call)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun createOngoingCallStyle(call: Call) =
        android.app.Notification.CallStyle.forOngoingCall(
            call.toPerson(),
            createActionIntent(
                NotificationButtonReceiver.Action.HANG_UP,
                pil.app.application,
            ),
        )

    @RequiresApi(Build.VERSION_CODES.S)
    private fun buildCallStyleNotification(
        call: Call,
    ) = buildBaseCallNotification(context, channelId).apply {
        setWhen(System.currentTimeMillis() - call.durationInSeconds)
        setShowWhen(true)
        setOngoing(true)
        setUsesChronometer(true)
        style = createOngoingCallStyle(call)
    }.build()

    private fun buildLegacyNotification(call: Call?) =
        NotificationCompat.Builder(context, channelId).apply {
            priority = NotificationCompat.PRIORITY_DEFAULT
            setContentTitle(call?.remotePartyHeading
                ?: context.getString(R.string.notification_default_title))
            setContentText(call?.prettyDuration
                ?: context.getString(R.string.notification_default_subtitle))
            setSmallIcon(R.drawable.ic_service)
            setWhen(System.currentTimeMillis())
            setShowWhen(true)
            setColorized(true)
            setOngoing(true)
            setAutoCancel(false)
            setCategory(android.app.Notification.CATEGORY_SERVICE)
            color = context.getColor(R.color.notification_background)
            addAction(
                R.drawable.ic_service,
                context.getString(R.string.notification_hang_up_action),
                createActionIntent(NotificationButtonReceiver.Action.HANG_UP,
                    pil.app.application)
            )
            setContentIntent(PendingIntent.getActivity(
                context,
                0,
                Intent(context, pil.app.activities.call),
                PendingIntent.FLAG_IMMUTABLE,
            ))
        }.build()

    companion object {
        private const val CHANNEL_ID = "VoIP"
    }
}

fun buildBaseCallNotification(
    context: Context,
    channelId: String,
): android.app.Notification.Builder =
    android.app.Notification.Builder(context, channelId).apply {
        setSmallIcon(R.drawable.ic_service)
        setCategory(android.app.Notification.CATEGORY_CALL)
        setContentIntent(PendingIntent.getActivity(
            context,
            0,
            Intent(context, PIL.instance.app.activities.call),
            PendingIntent.FLAG_IMMUTABLE
        ))
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

val Call.durationInSeconds: Int
    get() = duration * 1000
