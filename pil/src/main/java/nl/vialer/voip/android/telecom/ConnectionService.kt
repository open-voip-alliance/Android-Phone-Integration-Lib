package nl.vialer.voip.android.telecom

import android.annotation.SuppressLint
import android.telecom.Connection as AndroidConnection
import android.telecom.Connection.*
import android.telecom.ConnectionRequest
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.util.Log
import android.widget.Toast
import nl.vialer.voip.android.VoIPPIL
import nl.vialer.voip.android.startVoipService
import java.lang.Exception
import android.telecom.ConnectionService as AndroidConnectionService

class ConnectionService : AndroidConnectionService() {

    private val voip by lazy { VoIPPIL.instance }

    private val baseConnection: Connection
        get() = Connection(voip).apply {
            connectionProperties = PROPERTY_SELF_MANAGED
            connectionCapabilities = CAPABILITY_HOLD or CAPABILITY_SUPPORT_HOLD or CAPABILITY_MUTE
        }

    @SuppressLint("MissingPermission")
    override fun onCreateOutgoingConnection(connectionManagerPhoneAccount: PhoneAccountHandle, request: ConnectionRequest): Connection {
        val connection = baseConnection.apply {
            setCallerDisplayName("TODO: Actual Display Name", TelecomManager.PRESENTATION_ALLOWED)
            videoState = request.videoState
        }

        this.startVoipService(request.address.schemeSpecificPart)

        return connection.also { voip.connection = it }
    }

    override fun onCreateOutgoingConnectionFailed(connectionManagerPhoneAccount: PhoneAccountHandle?, request: ConnectionRequest?) {
        Toast.makeText(this, "Outgoing call failed", Toast.LENGTH_LONG).show()
    }

    override fun onCreateIncomingConnection(connectionManagerPhoneAccount: PhoneAccountHandle, request: ConnectionRequest): Connection {
        return baseConnection.apply {
            setCallerDisplayName("TODO: Actual Display Name", TelecomManager.PRESENTATION_ALLOWED)
            videoState = request.videoState
            setAddress(request.address, TelecomManager.PRESENTATION_ALLOWED)
        }.also { voip.connection = it }
    }

    override fun onCreateIncomingConnectionFailed(connectionManagerPhoneAccount: PhoneAccountHandle, request: ConnectionRequest) {
        Toast.makeText(this, "Incoming call failed", Toast.LENGTH_LONG).show()
    }
}