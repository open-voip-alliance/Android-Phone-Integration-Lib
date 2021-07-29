package org.openvoipalliance.androidphoneintegration.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.openvoipalliance.androidphoneintegration.PIL
import org.openvoipalliance.androidphoneintegration.di.di
import org.openvoipalliance.androidphoneintegration.telecom.AndroidCallFramework

internal class FcmService : FirebaseMessagingService() {

    private val pil: PIL by lazy { di.koin.get() }
    private val androidCallFramework: AndroidCallFramework by lazy { di.koin.get() }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        processRemoteMessage(remoteMessage)
    }

    private fun processRemoteMessage(remoteMessage: RemoteMessage) = GlobalScope.launch {
        if (pil.app.middleware?.inspect(remoteMessage) == false) {
            pil.writeLog("Client has inspected push message and determined this is not a call")
            return@launch
        }

        if (!PIL.isInitialized) return@launch

        pil.writeLog("Received FCM push message")

        if (androidCallFramework.isInCall) {
            pil.app.middleware?.respond(remoteMessage, false)
            return@launch
        }

        pil.start {
            pil.app.middleware?.respond(remoteMessage, true)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        if (!PIL.isInitialized) return

        pil.writeLog("Received new FCM token")

        pil.app.middleware?.tokenReceived(token)
    }
}
