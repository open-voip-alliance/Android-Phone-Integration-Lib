package org.openvoipalliance.androidplatformintegration.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.openvoipalliance.androidplatformintegration.PIL

internal class FcmService : FirebaseMessagingService() {

    private val pil by lazy { PIL.instance }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (pil.androidTelecomManager.isInCall) {
            pil.app.middleware?.respond(remoteMessage, false)
            return
        }

        pil.start {
            pil.app.middleware?.respond(remoteMessage, true)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        pil.app.middleware?.tokenReceived(token)
    }
}
