package org.openvoipalliance.androidphoneintegration.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import org.openvoipalliance.androidphoneintegration.PIL
import org.openvoipalliance.androidphoneintegration.di.di
import org.openvoipalliance.androidphoneintegration.service.NotificationButtonReceiver

internal abstract class Notification {

    protected val pil: PIL by di.koin.inject()
    protected val context: Context by di.koin.inject()
    protected val notificationManger: NotificationManager by di.koin.inject()

    protected abstract val channelId: String
    protected abstract val notificationId: Int

    protected abstract fun createNotificationChannel()

    fun cancel() {
        notificationManger.cancel(notificationId)
    }

    protected fun createActionIntent(action: NotificationButtonReceiver.Action, context: Context): PendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, NotificationButtonReceiver::class.java).apply {
            setAction(action.name)
        },
        0
    )
}