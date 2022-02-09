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

        pil.start()
    }

    override fun onApplicationEnterBackground(activity: Activity?) {
        pil.writeLog("Application has entered the background")
    }
}
