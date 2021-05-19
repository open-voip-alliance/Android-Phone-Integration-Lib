package org.openvoipalliance.androidphoneintegration

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.PowerManager
import android.os.PowerManager.*
import android.util.Log
import android.view.WindowManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import org.openvoipalliance.androidphoneintegration.events.Event
import org.openvoipalliance.androidphoneintegration.events.PILEventListener

class CallScreenLifecycleObserver(private val activity: Activity) : LifecycleObserver {

    private val keyguardManager by lazy { activity.getSystemService(KeyguardManager::class.java) }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun begin() {
        activity.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(true)
                setTurnScreenOn(true)
                keyguardManager.requestDismissKeyguard(this, null)
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
        if (activity is PILEventListener) {
            PIL.instance.events.stopListening(activity)
        }
    }
}
