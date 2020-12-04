package nl.vialer.voip.android.telecom

import android.annotation.SuppressLint
import android.telecom.Connection.*
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService as AndroidConnectionService
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.widget.Toast
import nl.vialer.voip.android.PIL

internal class ConnectionService : AndroidConnectionService() {

    private val pil by lazy { PIL.instance }

    private val baseConnection: Connection
        get() = Connection(pil).apply {
            connectionProperties = PROPERTY_SELF_MANAGED
            connectionCapabilities = CAPABILITY_HOLD or CAPABILITY_SUPPORT_HOLD or CAPABILITY_MUTE
            audioModeIsVoip = true
        }

    @SuppressLint("MissingPermission")
    override fun onCreateOutgoingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle,
        request: ConnectionRequest
    ): Connection {
        val connection = baseConnection.apply {
            videoState = request.videoState
        }

        pil.phoneLib.callTo(request.address.schemeSpecificPart)

        return connection.also { pil.connection = it }
    }

    override fun onCreateOutgoingConnectionFailed(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ) {
        Toast.makeText(this, "Outgoing call failed", Toast.LENGTH_LONG).show()
    }

    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle,
        request: ConnectionRequest
    ): Connection {
        return baseConnection.apply {
            videoState = request.videoState
            setCallerDisplayName(pil.call?.remotePartyHeading, TelecomManager.PRESENTATION_ALLOWED)
            setAddress(request.address, TelecomManager.PRESENTATION_ALLOWED)
        }.also { pil.connection = it }
    }

    override fun onCreateIncomingConnectionFailed(
        connectionManagerPhoneAccount: PhoneAccountHandle,
        request: ConnectionRequest
    ) {
        Toast.makeText(this, "Incoming call failed", Toast.LENGTH_LONG).show()
    }
}
