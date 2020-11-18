package nl.vialer.voip.android.example.ui.call

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import kotlinx.android.synthetic.main.activity_call.*
import nl.vialer.voip.android.R
import nl.vialer.voip.android.VoIPPIL
import nl.vialer.voip.android.events.Event

class CallActivity : AppCompatActivity() {

    private val voip by lazy { VoIPPIL.instance }

    private val renderUi = {
        displayCall()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)

        endCallButton.setOnClickListener {
            voip.endCall()
        }
    }

    override fun onResume() {
        super.onResume()

        displayCall()

        voip.eventListener = { event ->
            if (event == Event.CALL_ENDED) {
                finish()
            }
        }

        Handler().postDelayed(renderUi, 1000)
    }

    override fun onPause() {
        super.onPause()
        Handler().removeCallbacks(renderUi)
    }

    private fun displayCall() {
        val call = voip.call ?: return

        callTitle.text = call.remotePartyHeading
        callSubtitle.text = call.remotePartySubheading
        callDuration.text = call.prettyDuration

        Handler().postDelayed(renderUi, 1000)
    }
}