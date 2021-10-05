package org.openvoipalliance.androidphoneintegration.call

import android.annotation.SuppressLint
import org.openvoipalliance.androidphoneintegration.audio.LocalDtmfToneGenerator
import org.openvoipalliance.androidphoneintegration.telecom.AndroidCallFramework
import org.openvoipalliance.androidphoneintegration.telecom.Connection
import org.openvoipalliance.voiplib.VoIPLib
import org.openvoipalliance.voiplib.model.Call

class CallActions internal constructor(
    private val phoneLib: VoIPLib,
    private val calls: Calls,
    private val androidCallFramework: AndroidCallFramework,
    private val localDtmfToneGenerator: LocalDtmfToneGenerator
) {

    fun hold() {
        connection {
            it.onHold()
        }
    }

    fun unhold() {
        connection {
            it.onUnhold()
        }
    }

    fun toggleHold() {
        connection {
            it.toggleHold()
        }
    }

    /**
     * Send a string of DTMF, no local tone will be played.
     *
     */
    fun sendDtmf(dtmf: String) {
        callExists {
            phoneLib.actions(it).sendDtmf(dtmf)
        }
    }

    /**
     * Send a single DTMF character.
     *
     * @param playToneLocally If true, will play the requested DTMF tone to the local user.
     */
    fun sendDtmf(dtmf: Char, playToneLocally: Boolean = true) {
        if (playToneLocally) {
            localDtmfToneGenerator.play(dtmf)
        }

        sendDtmf(dtmf.toString())
    }

    @SuppressLint("MissingPermission")
    fun beginAttendedTransfer(number: String) {
        callExists {
            phoneLib.actions(it).beginAttendedTransfer(number)
        }
    }

    fun completeAttendedTransfer() {
        calls.transferSession?.let {
            phoneLib.actions(it.to).finishAttendedTransfer(it)
        }
    }

    @SuppressLint("MissingPermission")
    fun answer() {
        connection {
            it.onAnswer()
        }
    }

    @SuppressLint("MissingPermission")
    fun decline() {
        connection {
            it.onReject()
        }
    }

    fun end() {
        connection {
            it.onDisconnect()
        }
    }

    private fun connection(callback: (connection: Connection) -> Unit) {
        val connection = androidCallFramework.connection ?: return

        callback.invoke(connection)
    }

    /**
     * An easy way to perform a null safety check and log whether there was no call found.
     *
     */
    private fun callExists(callback: (call: Call) -> Unit) {
        callback.invoke(calls.activeVoipLibCall ?: return)
    }
}
