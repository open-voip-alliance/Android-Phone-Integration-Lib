package org.openvoipalliance.voiplib.repository

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.linphone.core.*
import org.linphone.core.GlobalState.Off
import org.linphone.core.GlobalState.On
import org.linphone.core.LogLevel.*
import org.openvoipalliance.voiplib.config.Config
import org.openvoipalliance.voiplib.model.Call
import org.openvoipalliance.voiplib.model.Codec
import org.openvoipalliance.voiplib.repository.initialise.LogLevel
import java.util.*
import org.linphone.core.Call as LinphoneCall

private const val LINPHONE_DEBUG_TAG = "SIMPLE_LINPHONE"

internal class LinphoneCoreInstanceManager(private val context: Context, private val dns: Dns): SimpleCoreListener, LoggingServiceListener {

    internal val state = CoreState()

    lateinit var voipLibConfig: Config
        internal set

    private var linphoneCore: Core? = null

    internal val logging: LoggingService
        get() = Factory.instance().loggingService

    val safeLinphoneCore: Core?
        get() {
            return if (state.initialised) {
                linphoneCore
            } else {
                Log.e(TAG, "Trying to get linphone core while not possible", Exception())
                null
            }
        }

    init {
        Factory.instance().setDebugMode(false, LINPHONE_DEBUG_TAG)
    }

    fun initialiseLinphone(config: Config) {
        this.voipLibConfig = config

        if (linphoneCore != null) {
            config.logListener?.onLogMessageWritten(LogLevel.WARNING, "Not initializing linphone, already initialized.")
            return
        }

        try {
            startLibLinphone()
        } catch (e: Exception) {
            config.logListener?.onLogMessageWritten(LogLevel.ERROR, "Failed to start Linphone: ${e.localizedMessage}")
            Log.e(TAG, "startLibLinphone: cannot start linphone")
        }
    }

    @Synchronized
    @Throws(Exception::class)
    private fun startLibLinphone() {
        logging.setLogLevel(Warning)

        voipLibConfig.logListener.let { logging.addListener(this) }

        this.linphoneCore = createLinphoneCore(context).also {
            applyPreStartConfiguration(it)
            it.start()
            applyPostStartConfiguration(it)
            configureCodecs(it)
            applyAdvancedVoipSettings(it)
            log("Started Linphone with config:\n ${it.config.dump()}")
        }

        state.destroyed = false
    }

    private fun applyPreStartConfiguration(core:Core) = core.apply {
        addListener(this@LinphoneCoreInstanceManager)
        isPushNotificationEnabled = false
        transports = transports.apply {
            udpPort = Port.DISABLED.value
            tcpPort = Port.DISABLED.value
            tlsPort = Port.RANDOM.value
        }
        mediaEncryption = MediaEncryption.SRTP
        isMediaEncryptionMandatory = true
        enableIpv6(false)
        enableDnsSrv(true)
        enableDnsSearch(false)
        setDnsServers(dns.getServers().also {
            log("Using DNS: ${it.joinToString(", ")}", LogLevel.MESSAGE)
        })
        setUserAgent(voipLibConfig.userAgent, null)
        maxCalls = 2
        ring = null
        isNativeRingingEnabled = false
        enableVideoDisplay(false)
        enableVideoCapture(false)
        isAutoIterateEnabled = true
        uploadBandwidth = Bandwidth.INFINITE.value
        downloadBandwidth = Bandwidth.INFINITE.value
        mtu = 1300
        guessHostname = true
        incTimeout = 60
        audioPort = Port.RANDOM.value
        nortpTimeout = 30
        avpfMode = AVPFMode.Disabled
        stunServer = voipLibConfig.stun
        natPolicy = natPolicy?.apply {
            enableStun(voipLibConfig.stun?.isNotEmpty() == true)
            enableUpnp(false)
        }
        audioJittcomp = 100
    }

    private fun applyPostStartConfiguration(core: Core) = core.apply {
        useInfoForDtmf = true
        useRfc2833ForDtmf = true
    }

    private fun applyAdvancedVoipSettings(core: Core) {
        log("Applying ${voipLibConfig.advancedVoIPSettings}")

        core.apply {
            enableEchoCancellation(voipLibConfig.advancedVoIPSettings.echoCancellation)
            enableAdaptiveRateControl(voipLibConfig.advancedVoIPSettings.adaptiveRateControl)
            mtu = voipLibConfig.advancedVoIPSettings.mtu
            adaptiveRateAlgorithm = voipLibConfig.advancedVoIPSettings.adaptiveRateAlgorithm.name.toLowerCase(Locale.ROOT)
            enableRtpBundle(voipLibConfig.advancedVoIPSettings.mediaMultiplexing)
            enableAudioAdaptiveJittcomp(voipLibConfig.advancedVoIPSettings.jitterCompensation)
        }
    }

    /**
     * Creates the Linphone core by reading in the linphone raw configuration file.
     *
     */
    private fun createLinphoneCore(context: Context)
    = Factory.instance().createCore("", "", context)

    internal fun log(message: String, level: LogLevel = LogLevel.DEBUG) {
        voipLibConfig.logListener?.onLogMessageWritten(message = message, lev = level)
    }

    private fun configureCodecs(core: Core) {
        val codecs = this.voipLibConfig.codecs

        core.videoPayloadTypes.forEach { it.enable(false) }

        core.audioPayloadTypes.forEach {
            it.enable(codecs.contains(Codec.valueOf(it.mimeType.toUpperCase(Locale.ROOT))))
        }

        log("Disabled codecs: " + core.audioPayloadTypes.filter { !it.enabled() }.joinToString(", ") { it.mimeType })
        log("Enabled codecs: " + core.audioPayloadTypes.filter { it.enabled() }.joinToString(", ") { it.mimeType })
    }

    override fun onCallStateChanged(lc: Core, linphoneCall: LinphoneCall, state: LinphoneCall.State, message: String) {
        GlobalScope.launch(Dispatchers.Main) {
            log("callState: $state, Message: $message, Duration = ${linphoneCall.duration}")

            preserveInviteData(linphoneCall)

            val call = Call(linphoneCall)

            when (state) {
                LinphoneCall.State.IncomingReceived -> voipLibConfig.callListener.incomingCallReceived(call)
                LinphoneCall.State.OutgoingInit -> voipLibConfig.callListener.outgoingCallCreated(call)
                LinphoneCall.State.Connected -> {
                    safeLinphoneCore?.activateAudioSession(true)
                    voipLibConfig.callListener.callConnected(call)
                }
                LinphoneCall.State.End, LinphoneCall.State.Error -> voipLibConfig.callListener.callEnded(call)
                LinphoneCall.State.Released -> voipLibConfig.callListener.callReleased(call)
                else -> voipLibConfig.callListener.callUpdated(call)
            }
        }
    }

    override fun onTransferStateChanged(lc: Core, transfered: org.linphone.core.Call, newCallState: org.linphone.core.Call.State) {
        voipLibConfig.callListener.attendedTransferMerged(Call(transfered))
    }

    /**
     * When placing a call on hold, certain INVITE information is lost,
     * this will ensure that the first value for a given call is preserved
     * so it is always available, even after being put on hold.
     *
     * The data is only updated when a new, non-null, non-blank value
     * is found.
     */
    private fun preserveInviteData(linphoneCall: org.linphone.core.Call) {
        if (linphoneCall.userData == null) {
            linphoneCall.userData = PreservedInviteData()
        }

        val userData = linphoneCall.userData as? PreservedInviteData ?: return

        userData.pAssertedIdentity = linphoneCall.remoteParams?.getCustomHeader("P-Asserted-Identity")
        userData.remotePartyId = linphoneCall.remoteParams?.getCustomHeader("Remote-Party-ID")
    }

    override fun onGlobalStateChanged(lc: Core, gstate: GlobalState, message: String) {
        gstate.let {
            globalState = it

            when (it) {
                Off -> voipLibConfig.onDestroy()
                On -> voipLibConfig.onReady()
                else -> {}
            }
        }
    }

    override fun onLogMessageWritten(service: LoggingService, domain: String, lev: org.linphone.core.LogLevel, message: String) {
        GlobalScope.launch(Dispatchers.IO) {
            voipLibConfig.logListener?.onLogMessageWritten(when (lev) {
                Debug -> LogLevel.DEBUG
                Trace -> LogLevel.TRACE
                Message -> LogLevel.MESSAGE
                Warning -> LogLevel.WARNING
                Error -> LogLevel.ERROR
                Fatal -> LogLevel.FATAL
            }, message)
        }
    }

    @Synchronized
    fun destroy() {
        state.destroyed = true
        state.isRegistered = false
        Factory.instance().loggingService.removeListener(this@LinphoneCoreInstanceManager)
        linphoneCore?.isNetworkReachable = false
        linphoneCore?.stop()
        linphoneCore?.removeListener(this@LinphoneCoreInstanceManager)
        linphoneCore = null
    }

    companion object {
        const val TAG = "VOIPLIB-LINPHONE"
        var globalState: GlobalState = Off
    }

    inner class CoreState {
        var destroyed: Boolean = false
        var isRegistered: Boolean = false
        val initialised: Boolean get() = linphoneCore != null && !destroyed
    }
}

enum class Port(val value: Int) {
    DISABLED(0), RANDOM(-1)
}

enum class Bandwidth(val value: Int) {
    INFINITE(0)
}

/**
 * Preserves certain information that is not necessarily
 * carried between call objects.
 */
internal class PreservedInviteData {
    var remotePartyId: String? = ""
        set(value) {
            if (value.isNullOrBlank()) return

            field = value
        }

    var pAssertedIdentity: String? = ""
        set(value) {
            if (value.isNullOrBlank()) return

            field = value
        }
}