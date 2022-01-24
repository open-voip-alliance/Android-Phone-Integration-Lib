package org.openvoipalliance.voiplib

import android.Manifest
import androidx.annotation.RequiresPermission
import org.linphone.core.AudioDevice
import org.linphone.core.Reason
import org.openvoipalliance.androidphoneintegration.di.di
import org.openvoipalliance.voiplib.model.AttendedTransferSession
import org.openvoipalliance.voiplib.model.Call
import org.openvoipalliance.voiplib.repository.call.controls.LinphoneSipActiveCallControlsRepository
import org.openvoipalliance.voiplib.repository.call.session.LinphoneSipSessionRepository

class Actions(private val call: Call) {

    private val sipCallControlsRepository: LinphoneSipActiveCallControlsRepository by di.koin.inject()
    private val sipSessionRepository: LinphoneSipSessionRepository by di.koin.inject()

    /** --Control an incoming call-- */
    /**
     * Accepts the current incoming session/call when it exists
     */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun accept() = sipSessionRepository.acceptIncoming(call)

    /**
     * Declines the current incoming session/call when it exists
     */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun decline(reason: Reason) = sipSessionRepository.declineIncoming(call, reason)

    /**
     * Hangs up the current active session/call when it exists
     */
    fun end() = sipSessionRepository.end(call)

    /**
     * Pause a session.
     */
    fun pause() = sipCallControlsRepository.pauseCall(call)

    /**
     * Resume a session.
     * @param call The session you want to resume.
     */
    fun resume() = sipCallControlsRepository.resumeCall(call)

    /**
     * Turns session on hold or off.
     * @param on If true, hold will turn on or stay on. If false it will turn off or stay off.
     */
    fun hold(on: Boolean) = sipCallControlsRepository.setHold(call, on)

    fun routeAudioToEarpiece(call: Call) =
        sipCallControlsRepository.routeAudioTo(
            arrayListOf(AudioDevice.Type.Earpiece),
            call.linphoneCall,
        )

    fun routeAudioToSpeaker(call: Call) =
        sipCallControlsRepository.routeAudioTo(
            arrayListOf(AudioDevice.Type.Speaker),
            call.linphoneCall,
        )

    fun routeAudioToBluetooth(call: Call) =
        sipCallControlsRepository.routeAudioTo(
            arrayListOf(AudioDevice.Type.Bluetooth),
            call.linphoneCall,
        )

    fun routeAudioToHeadset(call: Call) =
        sipCallControlsRepository.routeAudioTo(
            arrayListOf(AudioDevice.Type.Headphones, AudioDevice.Type.Headset),
            call.linphoneCall,
        )

    /**
     * Transfer a session to a number unattended.
     * @param to The number you want to call to.
     */
    fun transferUnattended(to: String) = sipCallControlsRepository.transferUnattended(call, to)

    /**
     * Begin an attended transfer, putting the current call on hold and placing a call to a new user.
     *
     * @param to The number you want to transfer to.
     */
    @RequiresPermission(allOf = [Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO])
    fun beginAttendedTransfer(to: String): AttendedTransferSession {
        pause()

        val targetCall = sipSessionRepository.callTo(to)

        sipCallControlsRepository.resumeCall(targetCall)

        return AttendedTransferSession(call, targetCall)
    }

    /**
     * Complete a pending attended transfer, merging the two calls.
     *
     * @param attendedTransferSession The transfer session that should be merged.
     */
    fun finishAttendedTransfer(attendedTransferSession: AttendedTransferSession) = sipCallControlsRepository.finishAttendedTransfer(attendedTransferSession)

    /**
     * Send a dtmf string.
     *
     */
    fun sendDtmf(dtmf: String) = sipCallControlsRepository.sendDtmf(call, dtmf)

    fun callInfo(): String = sipCallControlsRepository.provideCallInfo(call)
}