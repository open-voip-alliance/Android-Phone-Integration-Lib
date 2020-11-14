package nl.vialer.voip.android.telecom

import android.content.Context
import android.telecom.CallAudioState
import nl.vialer.voip.android.VoIPPIL
import org.openvoipalliance.phonelib.PhoneLib
import android.telecom.Connection as AndroidConnection

class Connection(voip: VoIPPIL) : AndroidConnection() {

    override fun onShowIncomingCallUi() {
        super.onShowIncomingCallUi()
    }

    override fun onCallAudioStateChanged(state: CallAudioState?) {
        super.onCallAudioStateChanged(state)
    }

    override fun onHold() {
        super.onHold()
    }

    override fun onUnhold() {
        super.onUnhold()
    }

    override fun onAnswer() {
        super.onAnswer()
    }

    override fun onReject() {
        super.onReject()
    }

    override fun onDisconnect() {
        super.onDisconnect()
    }
}