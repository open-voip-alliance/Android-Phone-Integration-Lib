package nl.vialer.voip.android.telecom

import android.annotation.SuppressLint
import android.content.Context
import android.telecom.CallAudioState
import android.telecom.DisconnectCause
import android.telecom.DisconnectCause.LOCAL
import android.util.Log
import nl.vialer.voip.android.VoIPPIL
import nl.vialer.voip.android.audio.AudioRoute
import nl.vialer.voip.android.audio.AudioState
import nl.vialer.voip.android.service.VoIPService
import nl.vialer.voip.android.service.startVoipService
import org.openvoipalliance.phonelib.PhoneLib
import org.openvoipalliance.phonelib.model.Reason
import android.telecom.Connection as AndroidConnection

class Connection(private val pil: VoIPPIL) : AndroidConnection() {

    override fun onShowIncomingCallUi() {
        if (!VoIPService.isRunning) {
            pil.context.startVoipService()
        }
    }

    @SuppressLint("MissingPermission", "NewApi") // TODO fix
    override fun onCallAudioStateChanged(state: CallAudioState) {
        Log.e("TEST123", "STate==${state.route}")
    }

    override fun onHold() {
        pil.callManager?.call?.let {
            pil.phoneLib.actions(it).hold(true)
            setOnHold()
        }
    }

    override fun onUnhold() {
        pil.callManager?.call?.let {
            pil.phoneLib.actions(it).hold(false)
            setActive()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onAnswer() {
        pil.callManager?.call?.let {
            pil.phoneLib.actions(it).accept()
            setActive()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onReject() {
        pil.callManager?.call?.let {
            pil.phoneLib.actions(it).decline(Reason.BUSY)
            destroy()
        }
    }

    override fun onDisconnect() {
        pil.callManager?.call?.let {
            pil.phoneLib.actions(it).end()
            setDisconnected(DisconnectCause(LOCAL))
            destroy()
        }
    }
}