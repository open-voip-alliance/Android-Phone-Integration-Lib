package org.openvoipalliance.androidphoneintegration.telecom

import android.annotation.SuppressLint
import android.telecom.Connection.*
import android.telecom.ConnectionRequest
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import org.openvoipalliance.androidphoneintegration.PIL
import org.openvoipalliance.androidphoneintegration.di.di
import org.openvoipalliance.androidphoneintegration.events.Event.CallSetupFailedEvent.IncomingCallSetupFailed
import org.openvoipalliance.androidphoneintegration.events.Event.CallSetupFailedEvent.OutgoingCallSetupFailed
import org.openvoipalliance.androidphoneintegration.events.Event.CallSetupFailedEvent.Reason.REJECTED_BY_ANDROID_TELECOM_FRAMEWORK
import org.openvoipalliance.androidphoneintegration.logWithContext
import org.openvoipalliance.androidphoneintegration.logging.LogLevel
import org.openvoipalliance.voiplib.VoIPLib
import android.telecom.ConnectionService as AndroidConnectionService

internal class ConnectionService : AndroidConnectionService() {

    private val pil: PIL by di.koin.inject()
    private val phoneLib: VoIPLib by di.koin.inject()
    private val androidCallFramework: AndroidCallFramework by di.koin.inject()

    @SuppressLint("MissingPermission")
    override fun onCreateOutgoingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle,
        request: ConnectionRequest,
    ) = connectionForRequest(request).also {
        phoneLib.callTo(request.address.schemeSpecificPart)
        log("Handled onCreateOutgoingConnection")
    }

    override fun onCreateOutgoingConnectionFailed(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?,
    ) {
        pil.events.broadcast(OutgoingCallSetupFailed(REJECTED_BY_ANDROID_TELECOM_FRAMEWORK))
        log("Unable to create outgoing connection", LogLevel.ERROR)
    }

    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle,
        request: ConnectionRequest,
    ) = connectionForRequest(request).also {
        log("Handled onCreateIncomingConnection")
    }

    override fun onCreateIncomingConnectionFailed(
        connectionManagerPhoneAccount: PhoneAccountHandle,
        request: ConnectionRequest,
    ) {
        pil.events.broadcast(IncomingCallSetupFailed(REJECTED_BY_ANDROID_TELECOM_FRAMEWORK))
        log("Unable to create incoming connection", LogLevel.ERROR)
    }

    /**
     * Create the connection object for this request, it will automatically use the connection
     * object we already have as it is very important we always hold onto that connection
     * object.
     */
    private fun connectionForRequest(request: ConnectionRequest) =
        (androidCallFramework.connection ?: di.koin.get(Connection::class)).apply {
            connectionProperties = PROPERTY_SELF_MANAGED
            connectionCapabilities = CAPABILITY_HOLD or CAPABILITY_SUPPORT_HOLD or CAPABILITY_MUTE
            audioModeIsVoip = true
            videoState = request.videoState
            setCallerDisplayName(pil.calls.active?.remotePartyHeading,
                TelecomManager.PRESENTATION_ALLOWED)
            setAddress(request.address, TelecomManager.PRESENTATION_ALLOWED)
        }.also {
            androidCallFramework.connection = it
        }

    private fun log(message: String, level: LogLevel = LogLevel.INFO) =
        logWithContext(message, "ANDROID-CONNECTION-SERVICE", level)
}
