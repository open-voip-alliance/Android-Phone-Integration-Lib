package org.openvoipalliance.androidphoneintegration.android

import android.telecom.DisconnectCause
import android.telecom.TelecomManager
import org.openvoipalliance.androidphoneintegration.PIL
import org.openvoipalliance.androidphoneintegration.call.Call
import org.openvoipalliance.androidphoneintegration.call.CallDirection
import org.openvoipalliance.androidphoneintegration.events.Event
import org.openvoipalliance.androidphoneintegration.events.Event.CallSessionEvent.*
import org.openvoipalliance.androidphoneintegration.events.PILEventListener
import org.openvoipalliance.androidphoneintegration.helpers.startCallActivity
import org.openvoipalliance.androidphoneintegration.helpers.startVoipService
import org.openvoipalliance.androidphoneintegration.helpers.stopVoipService
import org.openvoipalliance.androidphoneintegration.logging.LogLevel
import org.openvoipalliance.androidphoneintegration.notifications.MissedCallNotification
import org.openvoipalliance.androidphoneintegration.service.VoIPService
import org.openvoipalliance.androidphoneintegration.telecom.AndroidCallFramework

/**
 * Listens to PIL events and performs the necessary actions in the Android framework.
 *
 */
internal class PlatformIntegrator(private val pil: PIL, private val androidCallFramework: AndroidCallFramework) : PILEventListener {

    private fun handle(event: Event.CallSessionEvent, call: Call) = when(event) {

        is IncomingCallReceived -> {
            androidCallFramework.addNewIncomingCall(call.remoteNumber)
        }

        is OutgoingCallStarted -> {
            if (androidCallFramework.connection == null) {
                pil.writeLog("There is no connection object!", LogLevel.ERROR)
            }

            androidCallFramework.connection?.apply {
                setCallerDisplayName(
                    pil.calls.active?.remotePartyHeading,
                    TelecomManager.PRESENTATION_ALLOWED
                )
                setDialing()
            }

            pil.app.application.startCallActivity()
        }

        is CallConnected -> {
            if (!VoIPService.isRunning) {
                pil.writeLog("The VoIP service is not running, starting it.")
                pil.app.application.startVoipService()
            }

            pil.app.application.startCallActivity()
            androidCallFramework.connection?.setActive()
        }

        is CallEnded ->  {
            pil.app.application.stopVoipService()
            androidCallFramework.connection?.setDisconnected(DisconnectCause(DisconnectCause.REMOTE))
            androidCallFramework.connection?.destroy()
            androidCallFramework.connection = null
            notifyIfMissedCall(call)
        }

       else -> {}
    }

    private fun notifyIfMissedCall(call: Call) {
        if (call.duration > 0) return;

        if (call.direction != CallDirection.INBOUND) return;

        if (pil.app.notifyOnMissedCall) {
            MissedCallNotification().notify(call)
        }
    }

    override fun onEvent(event: Event) {
        if (event !is Event.CallSessionEvent) {
            return
        }

        pil.notifications.dismissStale()

        handle(event, event.state.activeCall ?: return)
    }
}