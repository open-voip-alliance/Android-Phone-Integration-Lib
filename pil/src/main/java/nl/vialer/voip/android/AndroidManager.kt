package nl.vialer.voip.android

import android.app.Activity
import com.takwolf.android.foreback.Foreback
import nl.vialer.voip.android.service.VoIPService

class AndroidManager(private val pil: VoIPPIL): Foreback.Listener {

    override fun onApplicationEnterForeground(activity: Activity?) {
        pil.start()
    }

    override fun onApplicationEnterBackground(activity: Activity?) {
        if (!VoIPService.isRunning) {
            pil.phoneLib.destroy()
        }
    }
}