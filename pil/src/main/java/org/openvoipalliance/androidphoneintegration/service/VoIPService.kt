package org.openvoipalliance.androidphoneintegration.service

import android.app.Notification
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import org.linphone.core.tools.service.CoreService
import org.openvoipalliance.androidphoneintegration.PIL
import org.openvoipalliance.androidphoneintegration.di.di
import org.openvoipalliance.androidphoneintegration.events.Event
import org.openvoipalliance.androidphoneintegration.events.PILEventListener
import org.openvoipalliance.androidphoneintegration.notifications.CallNotification
import org.openvoipalliance.androidphoneintegration.notifications.IncomingCallNotification
import java.util.*


internal class VoIPService : CoreService(), PILEventListener {

    private val pil by lazy { PIL.instance }

    private val callNotification: CallNotification by di.koin.inject()
    private val powerManager by lazy { getSystemService(PowerManager::class.java) }
    private val notificationManger: NotificationManager by di.koin.inject()

    private var timer: Timer? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private val handler by lazy { Handler(Looper.getMainLooper()) }

    private val callEventLoop = object : Runnable {
        override fun run() {
            if (pil.calls.isInCall)
                pil.events.broadcast(Event.CallSessionEvent.CallDurationUpdated::class)
            else
                stopSelf()

            handler.postDelayed(this, REPEAT_MS)
        }
    }

    override fun createServiceNotificationChannel() {
        callNotification.createNotificationChannel()
    }

    override fun showForegroundServiceNotification() {
        val notification = callNotification.build()

        showForegroundNotification(notification, callNotification.notificationId)

        pil.androidCallFramework.connection?.updateCurrentRouteBasedOnAudioState()
    }

    private fun showForegroundNotification(notification: Notification, id: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val types = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                FOREGROUND_SERVICE_TYPE_MICROPHONE or FOREGROUND_SERVICE_TYPE_PHONE_CALL
            } else {
                FOREGROUND_SERVICE_TYPE_PHONE_CALL
            }

            startForeground(id, notification, types)
        } else {
            startForeground(id, notification)
        }
    }

    override fun hideForegroundServiceNotification() {
        callNotification.cancel()
        stopForeground(true)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        pil.events.listen(this)

        pil.writeLog("Starting the VoIP Service and creating notification channels")
        enableProximitySensor()
        handler.post(callEventLoop)
        showForegroundServiceNotification()

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        pil.writeLog("Stopping VoIPService")

        timer?.cancel()
        handler.removeCallbacks(callEventLoop)
        disableProximitySensor()
        pil.events.stopListening(this)
        hideForegroundServiceNotification()
    }

    private fun enableProximitySensor() {
        if (wakeLock != null) return

        wakeLock = powerManager.newWakeLock(
            PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK
                    or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "Vialer::ProximitySensor"
        ).apply { acquire(2 * 60 * 60 * 1000) }
    }

    private fun disableProximitySensor() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }

            wakeLock = null
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        pil.writeLog("Task has been killed, ending calls and stopping pil.")
        pil.calls.forEach { _ -> pil.actions.end() }
    }

    private fun updateNotification() {
        // We'll check if we have an active incoming call notification, and if we do use that
        // instead of the ongoing call notification.
        val incomingCallNotification = notificationManger.activeNotifications.firstOrNull {
            it.id == IncomingCallNotification.NOTIFICATION_ID
        }

        if (incomingCallNotification != null) {
            showForegroundNotification(
                incomingCallNotification.notification,
                IncomingCallNotification.NOTIFICATION_ID,
            )
        } else {
            callNotification.update(pil.calls.active ?: return)
        }
    }

    override fun onEvent(event: Event) {
        updateNotification()

        if (event is Event.CallSessionEvent.CallConnected) {
            showForegroundServiceNotification()
        }
    }

    companion object {
        private const val REPEAT_MS = 500L
    }
}