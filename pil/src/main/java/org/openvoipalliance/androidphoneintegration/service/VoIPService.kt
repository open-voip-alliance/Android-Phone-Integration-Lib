package org.openvoipalliance.androidphoneintegration.service

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import org.linphone.core.tools.service.CoreService
import org.openvoipalliance.androidphoneintegration.PIL
import org.openvoipalliance.androidphoneintegration.di.di
import org.openvoipalliance.androidphoneintegration.events.Event
import org.openvoipalliance.androidphoneintegration.events.PILEventListener
import org.openvoipalliance.androidphoneintegration.notifications.CallNotification
import java.util.*

internal class VoIPService : CoreService(), PILEventListener {

    private val pil by lazy { PIL.instance }

    private val callNotification: CallNotification by di.koin.inject()
    private val powerManager by lazy { getSystemService(PowerManager::class.java) }

    private var timer: Timer? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private val handler by lazy { Handler(Looper.getMainLooper()) }

    private val callEventLoop = object : Runnable {
        override fun run() {
            if (pil.calls.active != null)
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
        callNotification.build(display = true)
    }

    override fun hideForegroundServiceNotification() {
        callNotification.cancel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        pil.events.listen(this)

        pil.writeLog("Starting the VoIP Service and creating notification channels")

        wakeLock = powerManager.newWakeLock(
            PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK
                    or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "Vialer::ProximitySensor"
        ).apply { acquire(2 * 60 * 60 * 1000) }

        handler.post(callEventLoop)

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        pil.writeLog("Stopping VoIPService")

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
    }

    override fun onEvent(event: Event) {
        callNotification.update(pil.calls.active ?: return)
    }

    companion object {
        private const val REPEAT_MS = 500L
    }
}