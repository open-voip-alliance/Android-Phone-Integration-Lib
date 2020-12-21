package org.openvoipalliance.androidplatformintegration.example.ui.call

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_call.callSubtitle
import kotlinx.android.synthetic.main.activity_call.callTitle
import kotlinx.android.synthetic.main.activity_incoming_call.*
import org.openvoipalliance.androidplatformintegration.PIL
import org.openvoipalliance.androidplatformintegration.example.R
import org.openvoipalliance.androidplatformintegration.CallScreenLifecycleObserver
import org.openvoipalliance.androidplatformintegration.events.Event
import org.openvoipalliance.androidplatformintegration.events.Event.*
import org.openvoipalliance.androidplatformintegration.events.PILEventListener

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
        val call = pil.call ?: return

        callTitle.text = call.remotePartyHeading
        callSubtitle.text = call.remotePartySubheading
    }

    override fun onEvent(event: Event) {
        when (event) {
            CALL_ENDED -> finish()
            CALL_UPDATED -> displayCall()
            else -> {}
        }
    }
}