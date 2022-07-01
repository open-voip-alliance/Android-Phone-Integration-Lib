package org.openvoipalliance.androidphoneintegration

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import org.openvoipalliance.androidphoneintegration.call.CallState
import org.openvoipalliance.androidphoneintegration.helpers.startCallActivity

internal class ApplicationStateListener(private val pil: PIL) : DefaultLifecycleObserver {

    override fun onStart(owner: LifecycleOwner) {
        isInForeground = true
        pil.writeLog("Application has entered the foreground")

        if (pil.calls.active?.state == CallState.CONNECTED) {
            pil.app.application.startCallActivity()
        }

        try {
            pil.start()
        } catch (e: Exception) {
            pil.writeLog("Unable to start PIL when entering foreground: ${e.localizedMessage}")
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        isInForeground = false
        pil.writeLog("Application has entered the background")
    }

    companion object {
        var isInForeground = false
    }
}
