package nl.vialer.voip.android.android

import android.app.Activity
import com.takwolf.android.foreback.Foreback
import nl.vialer.voip.android.PIL
import nl.vialer.voip.android.call.CallState
import nl.vialer.voip.android.service.VoIPService
import nl.vialer.voip.android.service.startCallActivity

internal class ApplicationStateListener(private val pil: PIL) : Foreback.Listener {

    override fun onApplicationEnterForeground(activity: Activity?) {
        pil.writeLog("Application has entered the foreground")

        if (VoIPService.isRunning && pil.app.automaticallyStartCallActivity) {
            if (pil.call?.state == CallState.CONNECTED) {
                pil.app.application.startCallActivity()
            }
        }

        try {
            pil.start()
        } catch (e: Exception) {
            pil.writeLog("Unable to start PIL when entering foreground")
        }
    }

    override fun onApplicationEnterBackground(activity: Activity?) {
        pil.writeLog("Application has entered the background")
    }
}
