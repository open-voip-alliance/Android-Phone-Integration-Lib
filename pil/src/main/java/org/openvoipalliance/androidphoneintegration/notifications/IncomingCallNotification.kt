package org.openvoipalliance.androidphoneintegration.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.annotation.ColorRes
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.openvoipalliance.androidphoneintegration.R
import org.openvoipalliance.androidphoneintegration.android.SilenceIncomingCallReceiver
import org.openvoipalliance.androidphoneintegration.call.Call
import org.openvoipalliance.androidphoneintegration.logWithContext
import org.openvoipalliance.androidphoneintegration.service.NotificationButtonReceiver

internal class IncomingCallNotification(private val incomingCallRinger: IncomingCallRinger) :
    Notification() {

    override val channelId = INCOMING_CALLS_CHANNEL_ID
    override val notificationId = 2435

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
        notificationManger.notify(notificationId, build(call, setOnlyAlertOnce))
        incomingCallRinger.start()
    }

    fun build(call: Call, setOnlyAlertOnce: Boolean = false): android.app.Notification =
        createNotificationChannel().run {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                buildCallStyleNotification(call, setOnlyAlertOnce)
            } else {
                buildLegacyNotification(call, setOnlyAlertOnce)
            }
        }

    private fun createFullScreenIntent(call: Call): PendingIntent? {
        val fullScreenIntent = Intent(context, pil.app.activities.incomingCall).apply {
            putExtra("is_incoming", true)
            putExtra("remote_party_heading", call.remotePartyHeading)
            putExtra("remote_party_subheading", call.remotePartySubheading)
            call.contact?.imageUri?.let {
                putExtra("remote_party_image_uri", it.toString())
            }
            flags = Intent.FLAG_ACTIVITY_NO_USER_ACTION or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        return PendingIntent.getActivity(
            context,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun buildNotificationStyle(call: Call) =
        android.app.Notification.CallStyle.forIncomingCall(
            call.toPerson(),
            createActionIntent(NotificationButtonReceiver.Action.DECLINE, pil.app.application),
            createActionIntent(NotificationButtonReceiver.Action.ANSWER, pil.app.application),
        )
            .setAnswerButtonColorHint(context.getColor(R.color.incoming_call_notification_answer_color))
            .setDeclineButtonColorHint(context.getColor(R.color.incoming_call_notification_decline_color))

    @RequiresApi(Build.VERSION_CODES.S)
    private fun buildCallStyleNotification(
        call: Call,
        setOnlyAlertOnce: Boolean = false,
    ) = buildBaseCallNotification(context, channelId).apply {
        setFullScreenIntent(createFullScreenIntent(call), true)
        setOnlyAlertOnce(setOnlyAlertOnce)
        style = buildNotificationStyle(call)
        setDeleteIntent(PendingIntent.getBroadcast(context, 0, Intent(
            context,
            SilenceIncomingCallReceiver::class.java
        ).apply {
            action =
                SILENCE_INCOMING_CALL_ACTION
        },
            PendingIntent.FLAG_IMMUTABLE))
    }.build()

    private fun buildLegacyNotification(
        call: Call,
        setOnlyAlertOnce: Boolean = false,
    ) = NotificationCompat.Builder(context, channelId).apply {
        setFullScreenIntent(createFullScreenIntent(call), true)
        setOnlyAlertOnce(setOnlyAlertOnce)
        setSmallIcon(R.drawable.ic_service)
        call.contact?.imageUri?.let {
            context.imageUriToBitmap(it)?.let { bitmap ->
                setLargeIcon(bitmap)
            }
        }
        setContentTitle(call.prettyRemoteParty)
        setCategory(android.app.Notification.CATEGORY_CALL)
        setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        setContentText(context.getString(R.string.notification_incoming_context_text))
        priority = NotificationCompat.PRIORITY_HIGH
        addAction(
            R.drawable.ic_service,
            context.createColoredActionText(R.string.notification_answer_action,
                R.color.incoming_call_notification_answer_color),
            createActionIntent(NotificationButtonReceiver.Action.ANSWER,
                pil.app.application)
        )
        addAction(
            R.drawable.ic_service,
            context.createColoredActionText(R.string.notification_decline_action,
                R.color.incoming_call_notification_decline_color),
            createActionIntent(NotificationButtonReceiver.Action.DECLINE,
                pil.app.application)
        )
    }.build().also {
        it.flags = it.flags or android.app.Notification.FLAG_INSISTENT
    }

    override fun cancel() {
        super.cancel()
        incomingCallRinger.stop()
        repeatBroadcastToEnsureIncomingCallActivityIsCancelled()
    }

    /**
     * We want to send this broadcast a few times to make sure the incoming call screen has been
     * properly cancelled, otherwise we might encounter a race-condition.
     *
     */
    private fun repeatBroadcastToEnsureIncomingCallActivityIsCancelled() {
        context.sendBroadcast(Intent(CANCEL_INCOMING_CALL_ACTION))

        CoroutineScope(IO).launch {
            repeat(10) {
                if (pil.calls.isInCall) return@repeat

                CoroutineScope(Main).launch {
                    context.sendBroadcast(Intent(CANCEL_INCOMING_CALL_ACTION))
                }
                delay(300)
            }
        }
    }

    private fun log(message: String) = logWithContext(message, "INCOMING-CALL-NOTIFICATION")

    companion object {
        private const val INCOMING_CALLS_CHANNEL_ID = "Incoming Calls"
        private const val CANCEL_INCOMING_CALL_ACTION =
            "org.openvoipalliance.androidphoneintegration.INCOMING_CALL_CANCEL"

        private const val SILENCE_INCOMING_CALL_ACTION =
            "org.openvoipalliance.androidphoneintegration.SILENCE_INCOMING_CALL_ACTION"
    }
}

private fun Context.createColoredActionText(@StringRes stringRes: Int, @ColorRes colorRes: Int) =
    SpannableString(getText(stringRes)).apply {
        setSpan(
            ForegroundColorSpan(getColor(colorRes)), 0, length, 0
        )
    }

private fun Context.imageUriToBitmap(imageUri: Uri): Bitmap? = try {
    if (Build.VERSION.SDK_INT < 28) {
        MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
    } else {
        val source: ImageDecoder.Source = ImageDecoder.createSource(contentResolver, imageUri)
        ImageDecoder.decodeBitmap(source)
    }
} catch (e: Exception) {
    null
}