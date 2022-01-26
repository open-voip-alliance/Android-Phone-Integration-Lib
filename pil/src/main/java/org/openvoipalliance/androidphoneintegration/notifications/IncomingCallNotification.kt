package org.openvoipalliance.androidphoneintegration.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import org.openvoipalliance.androidphoneintegration.R
import org.openvoipalliance.androidphoneintegration.call.Call
import org.openvoipalliance.androidphoneintegration.logWithContext
import org.openvoipalliance.androidphoneintegration.service.NotificationButtonReceiver

internal class IncomingCallNotification(private val incomingCallRinger: IncomingCallRinger): Notification() {

    override val channelId = INCOMING_CALLS_CHANNEL_ID
    override val notificationId = NOTIFICATION_ID

    override fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId,
            context.getString(R.string.notification_incoming_calls_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            setSound(null, null)
            setShowBadge(false)
        }

        notificationManger.createNotificationChannel(channel)
    }

    /**
     * Silence the incoming notification, stop the ringing.
     *
     */
    fun silence(call: Call) {
        notify(call, setOnlyAlertOnce = true)
        incomingCallRinger.stop()
        log("Silenced $channelId")
    }

    /**
     * Begin ringing the user's phone.
     *
     */
    fun notify(call: Call, setOnlyAlertOnce: Boolean = false) {
        log("Starting using CHANNEL: $channelId")
        createNotificationChannel()

        val fullScreenIntent = Intent(context, pil.app.activities.incomingCall).apply {
            putExtra("is_incoming", true)
            putExtra("remote_party_heading", call.remotePartyHeading)
            putExtra("remote_party_subheading", call.remotePartySubheading)
            flags = Intent.FLAG_ACTIVITY_NO_USER_ACTION or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = NotificationCompat.Builder(context, channelId).apply {
            setFullScreenIntent(fullScreenPendingIntent, true)
            setOnlyAlertOnce(setOnlyAlertOnce)
            setSmallIcon(R.drawable.ic_service)
            setContentTitle(call.prettyRemoteParty)
            setCategory(android.app.Notification.CATEGORY_CALL)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setContentText(context.getString(R.string.notification_incoming_context_text))
            priority = NotificationCompat.PRIORITY_HIGH
            addAction(
                R.drawable.ic_service,
                context.createColoredActionText(R.string.notification_answer_action, R.color.incoming_call_notification_answer_color),
                createActionIntent(NotificationButtonReceiver.Action.ANSWER, pil.app.application)
            )
            addAction(
                R.drawable.ic_service,
                context.createColoredActionText(R.string.notification_decline_action, R.color.incoming_call_notification_decline_color),
                createActionIntent(NotificationButtonReceiver.Action.DECLINE, pil.app.application)
            )
        }.build().also {
            it.flags = it.flags or android.app.Notification.FLAG_INSISTENT
        }

        notificationManger.notify(notificationId, notification)
        incomingCallRinger.start()
    }

    override fun cancel() {
        super.cancel()
        incomingCallRinger.stop()
        context.sendBroadcast(Intent(CANCEL_INCOMING_CALL_ACTION))
    }

    private fun log(message: String) = logWithContext(message, "INCOMING-CALL-NOTIFICATION")

    companion object {
        private const val INCOMING_CALLS_CHANNEL_ID = "Incoming Calls"
        private const val CANCEL_INCOMING_CALL_ACTION = "org.openvoipalliance.androidphoneintegration.INCOMING_CALL_CANCEL"
        private const val NOTIFICATION_ID = 676
    }
}

private fun Context.createColoredActionText(@StringRes stringRes: Int, @ColorRes colorRes: Int) =
    SpannableString(getText(stringRes)).apply {
        setSpan(
            ForegroundColorSpan(getColor(colorRes)), 0, length, 0
        )
    }