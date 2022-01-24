package org.openvoipalliance.androidphoneintegration

import android.app.Activity
import com.takwolf.android.foreback.Foreback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.openvoipalliance.androidphoneintegration.call.CallState
import org.openvoipalliance.androidphoneintegration.helpers.startCallActivity

internal class ApplicationStateListener(private val pil: PIL) : Foreback.Listener {

    override fun onApplicationEnterForeground(activity: Activity?) {
        pil.writeLog("Application has entered the foreground")

        if (pil.calls.active?.state == CallState.CONNECTED) {
            pil.app.application.startCallActivity()
        }

        CoroutineScope(Dispatchers.Main).launch {
            delay(SECONDS_TO_DELAY_START)
            try {
                pil.start()
            } catch (e: Exception) {
                pil.writeLog("Unable to start PIL when entering foreground: ${e.localizedMessage}")
            }
        }
    }

    override fun onApplicationEnterBackground(activity: Activity?) {
        pil.writeLog("Application has entered the background")
    }

    companion object {
        /**
         * Delay the starting by a few seconds to prevent clashes with registering
         * after push notification.
         */
        const val SECONDS_TO_DELAY_START = 3000L
    }
}
