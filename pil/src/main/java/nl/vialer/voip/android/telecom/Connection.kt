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
import org.openvoipalliance.phonelib.PhoneLib
import org.openvoipalliance.phonelib.model.Reason
import android.telecom.Connection as AndroidConnection

class Connection(private val voip: VoIPPIL) : AndroidConnection() {

    init {
        Log.e("TEST123", "INIT")
    }
    override fun onShowIncomingCallUi() {
        super.onShowIncomingCallUi()
    }

    @SuppressLint("MissingPermission", "NewApi") // TODO fix
    override fun onCallAudioStateChanged(state: CallAudioState) {
        Log.e("TEST123", "STate==${state.route}")
        voip.internalAudioState = AudioState(
                when (state.route) {
                    8 -> AudioRoute.SPEAKER
                    2 -> AudioRoute.BLUETOOTH
                    else -> AudioRoute.PHONE
                }, arrayOf(AudioRoute.PHONE), state.activeBluetoothDevice?.name ?: ""
        )
    }

    override fun onHold() {
        voip.phoneLibCall?.let {
            voip.phoneLib.actions(it).hold(true)
            setOnHold()
        }
    }

    override fun onUnhold() {
        voip.phoneLibCall?.let {
            voip.phoneLib.actions(it).hold(false)
            setActive()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onAnswer() {
        voip.phoneLibCall?.let {
            voip.phoneLib.actions(it).accept()
            setActive()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onReject() {
        voip.phoneLibCall?.let {
            voip.phoneLib.actions(it).decline(Reason.BUSY)
            destroy()
        }
    }

    override fun onDisconnect() {
        voip.phoneLibCall?.let {
            voip.phoneLib.actions(it).end()
            setDisconnected(DisconnectCause(LOCAL))
            destroy()
        }
    }
}