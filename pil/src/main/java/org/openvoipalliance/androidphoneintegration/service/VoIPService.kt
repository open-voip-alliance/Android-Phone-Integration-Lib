package org.openvoipalliance.androidphoneintegration.service

import android.annotation.SuppressLint
import android.app.*
import android.app.Notification.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import java.util.*
import org.openvoipalliance.androidphoneintegration.PIL
import org.openvoipalliance.androidphoneintegration.R

import org.openvoipalliance.androidphoneintegration.call.CallDirection
import org.openvoipalliance.androidphoneintegration.call.CallState
import org.openvoipalliance.androidphoneintegration.di.di
import org.openvoipalliance.androidphoneintegration.events.Event
import org.openvoipalliance.androidphoneintegration.events.PILEventListener
import org.openvoipalliance.androidphoneintegration.notifications.CallNotification

internal class VoIPService : Service(), PILEventListener {

    private val pil by lazy { PIL.instance }

    private val callNotification: CallNotification by di.koin.inject()
    private val powerManager by lazy { getSystemService(PowerManager::class.java) }

    private var timer: Timer? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private val handler = Handler()

    val callEventLoop = object : Runnable {
        override fun run() {
            if (pil.calls.active != null)
                pil.events.broadcast(Event.CallEvent.CallUpdated(pil.calls.active))
            else
                stopSelf()

            handler.postDelayed(this, REPEAT_MS)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isRunning = true
        pil.events.listen(this)

        pil.writeLog("Starting the VoIP Service and creating notification channels")

        startForeground()

        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "Vialer::IncomingCallWakelock"
        ).apply { acquire(30000) }

        handler.post(callEventLoop)

        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForeground() {
        pil.writeLog("Transitioning to a foreground service")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                callNotification.notificationId,
                callNotification.build().build(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
            )
        } else {
            startForeground(
                callNotification.notificationId,
                callNotification.build().build()
            )
        }

        callNotification.update(pil.calls.active ?: return)
    }

    override fun onDestroy() {
        super.onDestroy()
        pil.writeLog("Stopping VoIPService")

        isRunning = false
        timer?.cancel()
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }

            wakeLock = null
        }

        pil.events.stopListening(this)
    }

    override fun onEvent(event: Event) {
        callNotification.update(pil.calls.active ?: return)
    }

    override fun onBind(intent: Intent): IBinder? = null

    companion object {
        private const val REPEAT_MS = 500L
        internal var isRunning = false
    }
}