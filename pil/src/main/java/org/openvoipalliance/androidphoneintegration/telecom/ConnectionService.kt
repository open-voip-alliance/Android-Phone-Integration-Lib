package org.openvoipalliance.androidphoneintegration.telecom

import android.annotation.SuppressLint
import android.telecom.Connection.*
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService as AndroidConnectionService
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import org.openvoipalliance.androidphoneintegration.PIL
import org.openvoipalliance.androidphoneintegration.di.di
import org.openvoipalliance.androidphoneintegration.events.Event
import org.openvoipalliance.voiplib.VoIPLib

internal class ConnectionService : AndroidConnectionService() {

    private val pil: PIL by di.koin.inject()
    private val phoneLib: VoIPLib by di.koin.inject()
    private val androidCallFramework: AndroidCallFramework by di.koin.inject()

    private val baseConnection: Connection
        get() = di.koin.get(Connection::class).apply {
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

        phoneLib.callTo(request.address.schemeSpecificPart)

        return connection.also { androidCallFramework.connection = it }
    }

    override fun onCreateOutgoingConnectionFailed(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ) {
        pil.events.broadcast(Event.CallSetupFailedEvent.OutgoingCallSetupFailed(Event.CallSetupFailedEvent.Reason.REJECTED_BY_ANDROID_TELECOM_FRAMEWORK))
    }

    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle,
        request: ConnectionRequest
    ): Connection {
        return baseConnection.apply {
            videoState = request.videoState
            setCallerDisplayName(pil.calls.active?.remotePartyHeading, TelecomManager.PRESENTATION_ALLOWED)
            setAddress(request.address, TelecomManager.PRESENTATION_ALLOWED)
        }.also { androidCallFramework.connection = it }
    }

    override fun onCreateIncomingConnectionFailed(
        connectionManagerPhoneAccount: PhoneAccountHandle,
        request: ConnectionRequest
    ) {
        pil.events.broadcast(Event.CallSetupFailedEvent.IncomingCallSetupFailed(Event.CallSetupFailedEvent.Reason.REJECTED_BY_ANDROID_TELECOM_FRAMEWORK))
    }
}
