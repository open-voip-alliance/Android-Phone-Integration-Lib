package nl.vialer.voip.android.push

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import nl.vialer.voip.android.VoIPPIL

class FcmService: FirebaseMessagingService() {

    private val pil by lazy { VoIPPIL.instance }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        pil.start {
            pil.middlewareHandler?.respond(remoteMessage, true)
        }

    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        pil.middlewareHandler?.tokenReceived(token)
    }
}