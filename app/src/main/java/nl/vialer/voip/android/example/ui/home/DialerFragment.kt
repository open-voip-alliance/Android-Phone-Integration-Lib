package nl.vialer.voip.android.example.ui.home

import android.Manifest
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.fragment_dialer.*
import nl.vialer.voip.android.R
import nl.vialer.voip.android.PIL
import nl.vialer.voip.android.configuration.Auth
import nl.vialer.voip.android.events.Event
import nl.vialer.voip.android.events.PILEventListener
import nl.vialer.voip.android.example.ui.Dialer

class DialerFragment : Fragment(), PILEventListener {



    private val pil by lazy {
        PIL.instance
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_dialer, container, false)
        return root
    }

    override fun onResume() {
        super.onResume()
        pil.events.listen(this)
        requestCallingPermissions()
    }

    override fun onPause() {
        super.onPause()
        pil.events.stopListening(this)
    }

    override fun onEvent(event: Event) {
    }

    private fun requestCallingPermissions() {
        val requiredPermissions = arrayOf(Manifest.permission.CALL_PHONE, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_CONTACTS, Manifest.permission.READ_PHONE_STATE)

        requiredPermissions.forEach { permission ->
            if (ContextCompat.checkSelfPermission(requireActivity(), permission) == PERMISSION_DENIED) {
                requireActivity().requestPermissions(requiredPermissions, 101)
                return
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialer.onCallListener = Dialer.OnCallListener { number ->
            pil.call(number)
        }
    }
}