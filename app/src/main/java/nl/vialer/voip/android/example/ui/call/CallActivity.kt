package nl.vialer.voip.android.example.ui.call

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_call.*
import nl.vialer.voip.android.PIL
import nl.vialer.voip.android.example.R
import nl.vialer.voip.android.android.CallScreenLifecycleObserver
import nl.vialer.voip.android.audio.AudioRoute
import nl.vialer.voip.android.events.Event
import nl.vialer.voip.android.events.Event.*
import nl.vialer.voip.android.events.PILEventListener
import nl.vialer.voip.android.example.ui.TransferDialog

class CallActivity : AppCompatActivity(), PILEventListener {

    private val pil by lazy { PIL.instance }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)
        lifecycle.addObserver(CallScreenLifecycleObserver(this))

        endCallButton.setOnClickListener {
            pil.actions.end()
        }

        holdButton.setOnClickListener {
            pil.actions.toggleHold()
        }

        muteButton.setOnClickListener {
            pil.audio.toggleMute()
        }

        earpieceButton.setOnClickListener {
            pil.audio.routeAudio(AudioRoute.PHONE)
        }

        speakerButton.setOnClickListener {
            pil.audio.routeAudio(AudioRoute.SPEAKER)
        }

        bluetoothButton.setOnClickListener {
            pil.audio.routeAudio(AudioRoute.BLUETOOTH)
        }

        transferButton.setOnClickListener {
            TransferDialog(this).apply {
                onTransferListener = TransferDialog.OnTransferListener { number ->
                    pil.actions.beginAttendedTransfer(number)
                    dismiss()
                }
                show(supportFragmentManager, "")
            }
        }

        transferMergeButton.setOnClickListener {
            pil.actions.completeAttendedTransfer()
        }
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    override fun onDestroy() {
        super.onDestroy()
        pil.events.stopListening(this)
    }

    private fun render() {
        val call = pil.call ?: run {
            finish()
            return
        }

        if (pil.isInTransfer) {
            transferCallInformation.text = pil.transferCall?.remotePartyHeading
            if (pil.transferCall?.remotePartySubheading?.isNotBlank() == true) {
                transferCallInformation.text = "${transferCallInformation.text} (${pil.transferCall?.remotePartySubheading})"
            }
            transferContainer.visibility = View.VISIBLE
        } else {
            transferContainer.visibility = View.GONE
        }

        callTitle.text = call.remotePartyHeading
        callSubtitle.text = call.remotePartySubheading
        callDuration.text = call.prettyDuration

        holdButton.text = if (call.isOnHold) "unhold" else "hold"
        muteButton.text = if (pil.audio.isMicrophoneMuted) "unmute" else "mute"

        callStatus.text = call.state.name

        callDetailsAdvanced.text = ""

        earpieceButton.isEnabled = pil.audio.state.availableRoutes.contains(AudioRoute.PHONE)
        speakerButton.isEnabled = pil.audio.state.availableRoutes.contains(AudioRoute.SPEAKER)
        bluetoothButton.isEnabled = pil.audio.state.availableRoutes.contains(AudioRoute.BLUETOOTH)

        earpieceButton.setTypeface(null, if (pil.audio.state.currentRoute == AudioRoute.PHONE) Typeface.BOLD else Typeface.NORMAL)
        speakerButton.setTypeface(null, if (pil.audio.state.currentRoute == AudioRoute.SPEAKER) Typeface.BOLD else Typeface.NORMAL)
        bluetoothButton.setTypeface(null, if (pil.audio.state.currentRoute == AudioRoute.BLUETOOTH) Typeface.BOLD else Typeface.NORMAL)
        bluetoothButton.text = pil.audio.state.bluetoothDeviceName ?: "Bluetooth"
    }

    override fun onEvent(event: Event) = when (event) {
        CALL_ENDED -> {
            if (pil.call == null) {
                finish()
            } else {
                render()
            }
        }
        CALL_UPDATED -> render()
        else -> {}
    }
}
