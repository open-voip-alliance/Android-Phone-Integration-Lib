package nl.vialer.voip.android.example.ui.call

import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_call.*
import nl.vialer.voip.android.R
import nl.vialer.voip.android.VoIPPIL
import nl.vialer.voip.android.audio.AudioRoute
import nl.vialer.voip.android.events.Event
import nl.vialer.voip.android.events.EventListener
import nl.vialer.voip.android.example.ui.TransferDialog

class CallActivity : AppCompatActivity(), EventListener {

    private val voip by lazy { VoIPPIL.instance }

    private val renderUi = {
        displayCall()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)

        endCallButton.setOnClickListener {
            voip.actions.end()
        }

        holdButton.setOnClickListener {
            voip.actions.toggleHold()
        }

        muteButton.setOnClickListener {
            voip.audio.toggleMute()
        }

        earpieceButton.setOnClickListener {
            voip.audio.routeAudio(AudioRoute.PHONE)
        }

        speakerButton.setOnClickListener {
            voip.audio.routeAudio(AudioRoute.SPEAKER)
        }

        bluetoothButton.setOnClickListener {
            voip.audio.routeAudio(AudioRoute.BLUETOOTH)
        }

        transferButton.setOnClickListener {
            TransferDialog(this).apply {
                onTransferListener = TransferDialog.OnTransferListener { number ->
                    voip.actions.beginAttendedTransfer(number)
                    dismiss()
                }
                show(supportFragmentManager, "")
            }
        }

        transferMergeButton.setOnClickListener {
            voip.actions.completeAttendedTransfer()
        }
    }

    override fun onResume() {
        super.onResume()

        displayCall()

        voip.events.listen(this)

        Handler().postDelayed(renderUi, 1000)
    }

    override fun onPause() {
        super.onPause()
        voip.events.stopListening(this)
        Handler().removeCallbacks(renderUi)
    }

    private fun displayCall() {
        val call = voip.call ?: run {
            finish()
            return
        }

        if (voip.isInTransfer) {
            transferCallInformation.text = voip.transferCall?.remotePartyHeading
            if (voip.transferCall?.remotePartySubheading?.isNotBlank() == true) {
                transferCallInformation.text = "${transferCallInformation.text} (${voip.transferCall?.remotePartySubheading})"
            }
            transferContainer.visibility = View.VISIBLE
        } else {
            transferContainer.visibility = View.GONE
        }

        callTitle.text = call.remotePartyHeading
        callSubtitle.text = call.remotePartySubheading
        callDuration.text = call.prettyDuration

        holdButton.text = if (call.isOnHold) "unhold" else "hold"
        muteButton.text = if (voip.audio.isMicrophoneMuted) "unmute" else "mute"

        callStatus.text = call.state.name

        callDetailsAdvanced.text = ""

        earpieceButton.isEnabled = voip.audio.state.availableRoutes.contains(AudioRoute.PHONE)
        speakerButton.isEnabled = voip.audio.state.availableRoutes.contains(AudioRoute.SPEAKER)
        bluetoothButton.isEnabled = voip.audio.state.availableRoutes.contains(AudioRoute.BLUETOOTH)

        earpieceButton.setTypeface(null, if (voip.audio.state.currentRoute == AudioRoute.PHONE) Typeface.BOLD else Typeface.NORMAL)
        speakerButton.setTypeface(null, if (voip.audio.state.currentRoute == AudioRoute.SPEAKER) Typeface.BOLD else Typeface.NORMAL)
        bluetoothButton.setTypeface(null, if (voip.audio.state.currentRoute == AudioRoute.BLUETOOTH) Typeface.BOLD else Typeface.NORMAL)
        bluetoothButton.text = voip.audio.state.bluetoothDeviceName ?: "Bluetooth"

        Handler().postDelayed(renderUi, 1000)
    }

    override fun onEvent(event: Event) {
        if (event == Event.CALL_ENDED) {
            if (voip.call == null) {
                finish()
            } else {
                displayCall()
            }
        }

        if (event == Event.CALL_UPDATED) {
            displayCall()
        }
    }
}