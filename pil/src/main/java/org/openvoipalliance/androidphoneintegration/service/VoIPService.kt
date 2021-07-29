package org.openvoipalliance.androidphoneintegration.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.*
import org.openvoipalliance.androidphoneintegration.PIL
import org.openvoipalliance.androidphoneintegration.di.di
import org.openvoipalliance.androidphoneintegration.events.Event
import org.openvoipalliance.androidphoneintegration.events.PILEventListener
import org.openvoipalliance.androidphoneintegration.notifications.CallNotification
import java.util.*

internal class VoIPService : Service(), PILEventListener {

    private val pil by lazy { PIL.instance }

    private val callNotification: CallNotification by di.koin.inject()
    private val powerManager by lazy { getSystemService(PowerManager::class.java) }

    private var timer: Timer? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private val handler by lazy { Handler(Looper.getMainLooper()) }

    val callEventLoop = object : Runnable {
        override fun run() {
            if (pil.calls.active != null)
                pil.events.broadcast(Event.CallSessionEvent.CallDurationUpdated::class)
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
            PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK
                    or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "Vialer::ProximitySensor"
        ).apply { acquire(2 * 60 * 60 * 1000) }

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

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        pil.writeLog("Task has been killed, ending calls and stopping pil.")
        pil.calls.forEach { _ -> pil.actions.end() }
        pil.stop()
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