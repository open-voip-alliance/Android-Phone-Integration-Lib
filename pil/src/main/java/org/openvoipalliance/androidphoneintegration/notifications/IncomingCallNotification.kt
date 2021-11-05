package org.openvoipalliance.androidphoneintegration.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.app.NotificationCompat
import org.openvoipalliance.androidphoneintegration.R
import org.openvoipalliance.androidphoneintegration.call.Call
import org.openvoipalliance.androidphoneintegration.configuration.Preferences
import org.openvoipalliance.androidphoneintegration.service.NotificationButtonReceiver
import android.content.pm.PackageManager
import android.os.Build.VERSION_CODES

import android.os.Build.VERSION







internal class IncomingCallNotification: Notification() {

    private val preferences: Preferences
        get() = pil.preferences

    override val channelId: String
        get() = if (preferences.useApplicationProvidedRingtone) INCOMING_CALLS_APP_RING_CHANNEL_ID else INCOMING_CALLS_CHANNEL_ID

    override val notificationId = NOTIFICATION_ID

    private val ringtone: Uri
        get() = if (preferences.useApplicationProvidedRingtone) {
            Uri.parse("android.resource://${context.packageName}/raw/ringtone")
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        }

    override fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId,
            context.getString(R.string.notification_incoming_calls_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            setSound(
                ringtone,
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            setShowBadge(false)
        }

        notificationManger.createNotificationChannel(channel)
    }

    /**
     * Silence the incoming notification, stop the ringing.
     *
     */
    fun silence(call: Call) = notify(call, setOnlyAlertOnce = true)

    private fun launchIntent(context: Context): PendingIntent {

        val packageName = context.packageName
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getActivity(context, 1, intent, flags)
    }

    /**
     * Begin ringing the user's phone.
     *
     */
    fun notify(call: Call, setOnlyAlertOnce: Boolean = false) {
        createNotificationChannel()

        val notification = NotificationCompat.Builder(pil.app.application, channelId).apply {
            pil.app.activities.incomingCall?.let {
                val pendingIntent = launchIntent(context)
                setContentIntent(pendingIntent)
                setFullScreenIntent(pendingIntent, true)
            }
            setOngoing(true)
            setOnlyAlertOnce(setOnlyAlertOnce)
            setSmallIcon(R.drawable.ic_service)
            setContentTitle(call.remotePartyHeading)
            setCategory(android.app.Notification.CATEGORY_CALL)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setContentText(pil.app.application.getString(R.string.notification_incoming_context_text))
            color = pil.app.application.getColor(R.color.notification_background)
            setColorized(true)
            priority = NotificationCompat.PRIORITY_HIGH
            addAction(
                R.drawable.ic_service,
                pil.app.application.getString(R.string.notification_answer_action),
                createActionIntent(NotificationButtonReceiver.Action.ANSWER, pil.app.application)
            )
            addAction(
                R.drawable.ic_service,
                pil.app.application.getString(R.string.notification_decline_action),
                createActionIntent(NotificationButtonReceiver.Action.DECLINE, pil.app.application)
            )
        }.build().also {
            it.flags = it.flags or android.app.Notification.FLAG_INSISTENT
        }

        notificationManger.notify(notificationId, notification)
    }

    companion object {
        private const val INCOMING_CALLS_CHANNEL_ID = "VoIP Incoming Calls"
        private const val INCOMING_CALLS_APP_RING_CHANNEL_ID = "VoIP Incoming Calls (App Ring)"
        private const val NOTIFICATION_ID = 676
    }
}