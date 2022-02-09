package org.openvoipalliance.androidphoneintegration.helpers

import android.app.Application
import android.content.Context
import android.content.Intent
import com.takwolf.android.foreback.Foreback
import org.openvoipalliance.androidphoneintegration.PIL
import org.openvoipalliance.androidphoneintegration.configuration.ApplicationSetup.AutomaticallyLaunchCallActivity.NEVER
import org.openvoipalliance.androidphoneintegration.configuration.ApplicationSetup.AutomaticallyLaunchCallActivity.ONLY_FROM_BACKGROUND

fun Context.startCallActivity() {
    val automaticallyLaunchCallActivity = PIL.instance.app.automaticallyLaunchCallActivity

    if (automaticallyLaunchCallActivity == NEVER
        || (automaticallyLaunchCallActivity == ONLY_FROM_BACKGROUND && Foreback.isApplicationInTheForeground())) return

    PIL.instance.app.application.startActivity(
        Intent(PIL.instance.app.application, PIL.instance.app.activities.call).apply {
            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK
        }
    )
}

val Application.isInForeground
    get() = Foreback.isApplicationInTheForeground()