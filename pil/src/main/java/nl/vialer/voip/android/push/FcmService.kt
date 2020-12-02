package nl.vialer.voip.android.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import nl.vialer.voip.android.PIL

internal class FcmService: FirebaseMessagingService() {

    private val pil by lazy { PIL.instance }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (pil.androidTelecomManager.isInCall) {
            pil.application.middleware?.respond(remoteMessage, false)
            return
        }

        pil.start {
            pil.application.middleware?.respond(remoteMessage, true)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        pil.application.middleware?.tokenReceived(token)
    }
}