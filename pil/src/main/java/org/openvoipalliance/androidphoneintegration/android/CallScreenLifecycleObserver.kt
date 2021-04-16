package org.openvoipalliance.androidphoneintegration

import android.app.Activity
import android.media.AudioManager
import android.os.Build
import android.os.PowerManager
import android.os.PowerManager.*
import android.view.WindowManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import org.openvoipalliance.androidphoneintegration.events.PILEventListener

class CallScreenLifecycleObserver(private val activity: Activity) : LifecycleObserver {

    private val powerManager by lazy { activity.getSystemService(PowerManager::class.java) }

    private var wakeLock: WakeLock? = null

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun begin() {
        wakeLock = powerManager.newWakeLock(
            PROXIMITY_SCREEN_OFF_WAKE_LOCK or ACQUIRE_CAUSES_WAKEUP,
            "${activity.packageName}:call"
        )
            .apply {
                acquire(10 * 60 * 1000L)
            }

        activity.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(true)
                setTurnScreenOn(true)
            } else {
                window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
            }
            volumeControlStream = AudioManager.STREAM_VOICE_CALL
        }

        if (activity is PILEventListener) {
            PIL.instance.events.listen(activity)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun end() {
        wakeLock?.release()
        wakeLock = null

        if (activity is PILEventListener) {
            PIL.instance.events.stopListening(activity)
        }
    }
}
