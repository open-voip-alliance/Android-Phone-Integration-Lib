package nl.vialer.voip.android.example.ui.call

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_call.callSubtitle
import kotlinx.android.synthetic.main.activity_call.callTitle
import kotlinx.android.synthetic.main.activity_incoming_call.*
import nl.vialer.voip.android.R
import nl.vialer.voip.android.PIL
import nl.vialer.voip.android.events.Event
import nl.vialer.voip.android.events.PILEventListener

class IncomingCallActivity : AppCompatActivity(), PILEventListener {

    private val pil by lazy { PIL.instance }

    private val renderUi = {
        displayCall()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incoming_call)

        answerCallButton.setOnClickListener {
            pil.actions.answer()
        }

        declineCallButton.setOnClickListener {
            pil.actions.decline()
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
    }

    override fun onResume() {
        super.onResume()

        displayCall()

        pil.events.listen(this)

        Handler().postDelayed(renderUi, 1000)
    }

    override fun onPause() {
        super.onPause()
        pil.events.stopListening(this)
        Handler().removeCallbacks(renderUi)
    }

    private fun displayCall() {
        val call = pil.call ?: return

        callTitle.text = call.remotePartyHeading
        callSubtitle.text = call.remotePartySubheading

        Handler().postDelayed(renderUi, 1000)
    }

    override fun onEvent(event: Event) {
        if (event == Event.CALL_ENDED) {
            finish()
        }

        if (event == Event.CALL_UPDATED) {
            displayCall()
        }
    }
}