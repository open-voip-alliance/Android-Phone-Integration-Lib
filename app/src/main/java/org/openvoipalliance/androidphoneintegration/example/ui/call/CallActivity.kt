package org.openvoipalliance.androidphoneintegration.example.ui.call

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_call.*
import org.openvoipalliance.androidphoneintegration.android.CallScreenLifecycleObserver
import org.openvoipalliance.androidphoneintegration.PIL
import org.openvoipalliance.androidphoneintegration.audio.AudioRoute
import org.openvoipalliance.androidphoneintegration.events.Event
import org.openvoipalliance.androidphoneintegration.events.Event.CallSessionEvent
import org.openvoipalliance.androidphoneintegration.events.PILEventListener
import org.openvoipalliance.androidphoneintegration.example.R
import org.openvoipalliance.androidphoneintegration.example.ui.TransferDialog

class CallActivity : AppCompatActivity(), PILEventListener {

    private val pil by lazy { PIL.instance }

    private var lastEvent: CallSessionEvent? = null

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
            val bluetoothRoutes = pil.audio.state.bluetoothRoutes

            if (bluetoothRoutes.size > 1) {
                AlertDialog.Builder(this)
                    .setItems(bluetoothRoutes.map { it.displayName }.toTypedArray()) { _, which ->
                        pil.audio.routeAudio(bluetoothRoutes[which])
                    }
                    .show()

                return@setOnClickListener
            }

            pil.audio.routeAudio(AudioRoute.BLUETOOTH)
        }

        transferButton.setOnClickListener {
            pil.actions.hold()
            TransferDialog().apply {
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

        dtmfButton.setOnClickListener {

            val editText = EditText(this).apply {
                inputType = InputType.TYPE_CLASS_NUMBER
            }

            AlertDialog.Builder(this).apply {
                setView(editText)
                setTitle("Send DTMF to Remote Party")
                setPositiveButton("Send DTMF") { _, _ ->
                    val dtmf: String = editText.text.toString()

                    if (dtmf.length > 1) {
                        pil.actions.sendDtmf(dtmf)
                    } else {
                        pil.actions.sendDtmf(dtmf[0])
                    }
                }
                setNegativeButton("Cancel") { _, _ ->
                }
            }.show()
        }
    }

    override fun onResume() {
        super.onResume()
        lastEvent?.let { render(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        pil.events.stopListening(this)
    }

    @SuppressLint("SetTextI18n")
    private fun render(event: CallSessionEvent) {
        if (event is CallSessionEvent.CallDurationUpdated) {
            callDuration.text = event.state.activeCall?.prettyDuration
            return
        }

        if (event is CallSessionEvent.AttendedTransferEvent.AttendedTransferStarted) {
            transferContainer.visibility = View.VISIBLE
        } else if (event is CallSessionEvent.AttendedTransferEvent.AttendedTransferAborted) {
            transferContainer.visibility = View.GONE
        }

        val call = event.state.activeCall ?: return

        transferCallInformation.text = pil.calls.inactive?.remotePartyHeading
        if (pil.calls.inactive?.remotePartySubheading?.isNotBlank() == true) {
            transferCallInformation.text = "${transferCallInformation.text} (${pil.calls.inactive?.remotePartySubheading})"
        }

        callTitle.text = call.remotePartyHeading
        callSubtitle.text = call.remotePartySubheading
        callDuration.text = event.state.activeCall?.prettyDuration

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
        is CallSessionEvent.CallEnded -> finish()
        is CallSessionEvent.AttendedTransferEvent.AttendedTransferEnded -> {
            Toast.makeText(this, "Call transfer complete!", Toast.LENGTH_LONG).show()
            finish()
        }
        is CallSessionEvent -> render(event)
        else -> {}
    }
}
