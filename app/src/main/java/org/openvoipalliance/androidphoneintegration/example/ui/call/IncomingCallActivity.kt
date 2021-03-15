package org.openvoipalliance.androidphoneintegration.example.ui.call

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_call.callSubtitle
import kotlinx.android.synthetic.main.activity_call.callTitle
import kotlinx.android.synthetic.main.activity_incoming_call.*
import org.openvoipalliance.androidphoneintegration.CallScreenLifecycleObserver
import org.openvoipalliance.androidphoneintegration.PIL
import org.openvoipalliance.androidphoneintegration.events.Event
import org.openvoipalliance.androidphoneintegration.events.Event.CallEvent
import org.openvoipalliance.androidphoneintegration.events.PILEventListener
import org.openvoipalliance.androidphoneintegration.example.R

class IncomingCallActivity : AppCompatActivity(), PILEventListener {

    private val pil by lazy { PIL.instance }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incoming_call)
        lifecycle.addObserver(CallScreenLifecycleObserver(this))

        answerCallButton.setOnClickListener {
            pil.actions.answer()
        }

        declineCallButton.setOnClickListener {
            pil.actions.decline()
        }
    }

    override fun onResume() {
        super.onResume()
        displayCall()
    }

    private fun displayCall() {
        val call = pil.calls.active ?: return

        callTitle.text = call.remotePartyHeading
        callSubtitle.text = call.remotePartySubheading
    }

    override fun onEvent(event: Event) {
        when (event) {
            is CallEvent.CallEnded -> finish()
            is CallEvent.CallUpdated -> displayCall()
            else -> {}
        }
    }
}
