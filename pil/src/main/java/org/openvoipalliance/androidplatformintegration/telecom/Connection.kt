package org.openvoipalliance.androidplatformintegration.telecom

import android.annotation.SuppressLint
import android.telecom.Connection as AndroidConnection
import android.telecom.DisconnectCause
import android.telecom.DisconnectCause.LOCAL
import org.openvoipalliance.androidplatformintegration.call.CallManager
import org.openvoipalliance.androidplatformintegration.PIL
import org.openvoipalliance.androidplatformintegration.events.Event
import org.openvoipalliance.androidplatformintegration.service.VoIPService
import org.openvoipalliance.androidplatformintegration.service.startVoipService
import org.openvoipalliance.voiplib.VoIPLib
import org.openvoipalliance.voiplib.model.Call
import org.openvoipalliance.voiplib.model.Reason

class Connection internal constructor(
    private val pil: PIL,
    private val phoneLib: VoIPLib,
    private val callManager: CallManager,
    private val androidCallFramework: AndroidCallFramework
    ) : AndroidConnection() {

    override fun onShowIncomingCallUi() {
        if (!VoIPService.isRunning) {
            pil.app.application.startVoipService()
        }
    }

    override fun onHold() {
        callExists {
            phoneLib.actions(it).hold(true)
            setOnHold()
        }
    }

    fun toggleHold() {
        callExists {
            phoneLib.actions(it).hold(!it.isOnHold)
        }
    }

    override fun onUnhold() {
        callExists {
            phoneLib.actions(it).hold(false)
            setActive()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onAnswer() {
        callExists {
            phoneLib.actions(it).accept()
            setActive()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onReject() {
        callExists {
            phoneLib.actions(it).decline(Reason.BUSY)
            destroy()
        }
    }

    override fun onDisconnect() {
        callExists {
            phoneLib.actions(it).end()
        }

        androidCallFramework.connection = null
        setDisconnected(DisconnectCause(LOCAL))
        destroy()
    }

    /**
     * An easy way to perform a null safety check and log whether there was no call found.
     *
     */
    private fun callExists(callback: (call: Call) -> Unit) {
        var call = callManager.call ?: return

        callManager.transferSession?.let {
            call = it.to
        }

        callback.invoke(call)
        pil.events.broadcast(Event.CallEvent.CallUpdated(pil.calls.active))
    }
}
