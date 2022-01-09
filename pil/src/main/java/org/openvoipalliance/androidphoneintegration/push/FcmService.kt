package org.openvoipalliance.androidphoneintegration.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.openvoipalliance.androidphoneintegration.PIL
import org.openvoipalliance.androidphoneintegration.di.di
import org.openvoipalliance.androidphoneintegration.logWithContext
import org.openvoipalliance.androidphoneintegration.push.UnavailableReason.*
import org.openvoipalliance.androidphoneintegration.telecom.AndroidCallFramework

internal class FcmService : FirebaseMessagingService() {

    private val pil: PIL by lazy { di.koin.get() }
    private val androidCallFramework: AndroidCallFramework by lazy { di.koin.get() }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (!PIL.isInitialized) return

        val middleware = pil.app.middleware ?: return

        if (!middleware.inspect(remoteMessage)) {
            log("Client has inspected push message and determined this is not a call")
            return
        }

        log("Received FCM push message")

        if (androidCallFramework.isInCall) {
            log("Currently in call, rejecting incoming call")
            middleware.respond(remoteMessage, false, IN_CALL)
            return
        }

        if (!androidCallFramework.canHandleIncomingCall) {
            log("The android call framework cannot handle incoming call, responding as unavailable")
            middleware.respond(remoteMessage, false, REJECTED_BY_ANDROID_TELECOM_FRAMEWORK)
            return
        }

        pil.start { success ->
            when (success) {
                true -> middleware.respond(remoteMessage, true)
                false -> middleware.respond(remoteMessage, false, UNABLE_TO_REGISTER)
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        if (!PIL.isInitialized) return

        log("Received new FCM token")

        pil.app.middleware?.tokenReceived(token)
    }

    private fun log(message: String) = logWithContext(message, "FCM-SERVICE")
}

enum class UnavailableReason {
    IN_CALL,
    REJECTED_BY_ANDROID_TELECOM_FRAMEWORK,
    UNABLE_TO_REGISTER,
}
