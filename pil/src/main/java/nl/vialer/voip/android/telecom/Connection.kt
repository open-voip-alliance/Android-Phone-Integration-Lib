package nl.vialer.voip.android.telecom

import android.annotation.SuppressLint
import android.telecom.DisconnectCause
import android.telecom.DisconnectCause.LOCAL
import nl.vialer.voip.android.PIL
import nl.vialer.voip.android.events.Event
import nl.vialer.voip.android.service.VoIPService
import nl.vialer.voip.android.service.startVoipService
import org.openvoipalliance.phonelib.model.Call
import org.openvoipalliance.phonelib.model.Reason
import android.telecom.Connection as AndroidConnection

class Connection internal constructor(private val pil: PIL) : AndroidConnection() {

    override fun onShowIncomingCallUi() {
        if (!VoIPService.isRunning) {
            pil.app.application.startVoipService()
        }
    }

    override fun onHold() {
        callExists {
            pil.phoneLib.actions(it).hold(true)
            setOnHold()
        }
    }

    fun toggleHold() {
        callExists {
            pil.phoneLib.actions(it).hold(!it.isOnHold)
        }
    }

    override fun onUnhold() {
        callExists {
            pil.phoneLib.actions(it).hold(false)
            setActive()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onAnswer() {
        callExists {
            pil.phoneLib.actions(it).accept()
            setActive()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onReject() {
        callExists {
            pil.phoneLib.actions(it).decline(Reason.BUSY)
            destroy()
        }
    }

    override fun onDisconnect() {
        callExists {
            pil.phoneLib.actions(it).end()
        }

        pil.connection = null
        setDisconnected(DisconnectCause(LOCAL))
        destroy()
    }

    /**
     * An easy way to perform a null safety check and log whether there was no call found.
     *
     */
    private fun callExists(callback: (call: Call) -> Unit) {
        var call = pil.callManager.call ?: return

        pil.callManager.transferSession?.let {
            call = it.to
        }

        callback.invoke(call)
        pil.events.broadcast(Event.CALL_UPDATED)
    }
}