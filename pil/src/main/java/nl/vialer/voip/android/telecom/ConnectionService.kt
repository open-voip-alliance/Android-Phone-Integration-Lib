package nl.vialer.voip.android.telecom

import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.PhoneAccountHandle
import android.telecom.ConnectionService as AndroidConnectionService

class ConnectionService : AndroidConnectionService() {

    override fun onCreateOutgoingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection {
        return super.onCreateOutgoingConnection(connectionManagerPhoneAccount, request)
    }

    override fun onCreateOutgoingConnectionFailed(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ) {
        super.onCreateOutgoingConnectionFailed(connectionManagerPhoneAccount, request)
    }

    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection {
        return super.onCreateIncomingConnection(connectionManagerPhoneAccount, request)
    }

    override fun onCreateIncomingConnectionFailed(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ) {
        super.onCreateIncomingConnectionFailed(connectionManagerPhoneAccount, request)
    }
}