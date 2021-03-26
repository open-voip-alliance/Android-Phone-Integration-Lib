package org.openvoipalliance.androidphoneintegration.helpers

import android.content.Context
import android.content.Intent
import org.openvoipalliance.androidphoneintegration.PIL
import org.openvoipalliance.androidphoneintegration.service.VoIPService

fun Context.startVoipService() {
    startForegroundService(Intent(this, VoIPService::class.java))
}

fun Context.stopVoipService() {
    stopService((Intent(this, VoIPService::class.java)))
}

fun Context.startCallActivity() {
    if (!PIL.instance.app.automaticallyStartCallActivity) return

    PIL.instance.app.application.startActivity(
        Intent(PIL.instance.app.application, PIL.instance.app.activities.call).apply {
            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK
        }
    )
}
