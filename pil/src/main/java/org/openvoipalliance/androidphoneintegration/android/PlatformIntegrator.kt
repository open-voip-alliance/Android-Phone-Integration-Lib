package org.openvoipalliance.androidphoneintegration.android

import android.telecom.DisconnectCause
import android.telecom.TelecomManager
import org.openvoipalliance.androidphoneintegration.PIL
import org.openvoipalliance.androidphoneintegration.call.Call
import org.openvoipalliance.androidphoneintegration.call.CallFactory
import org.openvoipalliance.androidphoneintegration.call.VoipLibCall
import org.openvoipalliance.androidphoneintegration.events.Event
import org.openvoipalliance.androidphoneintegration.events.Event.CallSessionEvent.*
import org.openvoipalliance.androidphoneintegration.events.PILEventListener
import org.openvoipalliance.androidphoneintegration.helpers.startCallActivity
import org.openvoipalliance.androidphoneintegration.logging.LogLevel
import org.openvoipalliance.androidphoneintegration.notifications.MissedCallNotification
import org.openvoipalliance.androidphoneintegration.telecom.AndroidCallFramework

/**
 * Listens to PIL events and performs the necessary actions in the Android framework.
 *
 */
internal class PlatformIntegrator(
    private val pil: PIL, private val
    androidCallFramework: AndroidCallFramework,
    private val factory: CallFactory,
) : PILEventListener {

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
            pil.app.application.startCallActivity()
        }

        is CallEnded -> {
            pil.audio.unmute()
            androidCallFramework.connection?.setDisconnected(DisconnectCause(DisconnectCause.REMOTE))
            androidCallFramework.connection?.destroy()
            androidCallFramework.connection = null
        }

       else -> {}
    }

    internal fun notifyIfMissedCall(call: VoipLibCall) {
        if (call.wasMissed && pil.app.notifyOnMissedCall) {
            factory.make(call)?.let {
                MissedCallNotification().notify(it)
            }
        }
    }

    override fun onEvent(event: Event) {
        pil.notifications.dismissStale()

        if (event !is Event.CallSessionEvent) {
            return
        }

        handle(event, event.state.activeCall ?: return)
    }
}