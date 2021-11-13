package org.openvoipalliance.androidphoneintegration.voip.linphone

import android.content.Context
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.linphone.core.*
import org.openvoipalliance.androidphoneintegration.PIL
import org.openvoipalliance.androidphoneintegration.log
import org.openvoipalliance.androidphoneintegration.logging.LogLevel
import java.util.*

class LinphoneManager(private val pil: PIL, private val listener: LinphoneListener) {

    private var core: Core? = null

    private val isInitialized: Boolean
        get() = core != null

    internal val logging: LoggingService
        get() = Factory.instance().loggingService

    init {
        Factory.instance().setDebugMode(false, "APL - LinphoneManager")
    }

    fun initializeLinphone() {
        if (isInitialized) {
            log("Unable to initialize Linphone because it is already started!")
            return
        }

        try {
            startLinphone()
        } catch (e: Exception) {
            log("Failed to start Linphone ${e.localizedMessage}", LogLevel.ERROR)
        }
    }

    private fun startLinphone() {
        logging.setLogLevel(org.linphone.core.LogLevel.Warning)

        this.core = createLinphoneCore(pil.app.application).also {
            applyPreStartConfiguration(it)
            it.start()
            applyPostStartConfiguration(it)
            configureCodecs(it)
            log("Started Linphone with config:\n ${it.config.dump()}")
        }
    }

    private fun applyPreStartConfiguration(core:Core) = core.apply {
        addListener(listener)
        isPushNotificationEnabled = false
        transports = transports.apply {
            tlsPort = Port.DISABLED.value
            udpPort = Port.DISABLED.value
            tcpPort = Port.DISABLED.value
        }
        enableIpv6(false)
        enableDnsSrv(false)
        enableDnsSearch(false)
        setUserAgent(pil.app.userAgent, null)
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
        stunServer = ""
        natPolicy = natPolicy?.apply {
            enableStun(false)
            enableUpnp(false)
        }
        audioJittcomp = 100
    }

    private fun applyPostStartConfiguration(core: Core) = core.apply {
        useInfoForDtmf = true
        useRfc2833ForDtmf = true
        enableEchoCancellation(true)
        enableAdaptiveRateControl(true)
        enableAudioAdaptiveJittcomp(true)
        mtu = 1300
        enableRtpBundle(false)
    }

    private fun configureCodecs(core: Core) {
        val codecs = pil.preferences.codecs

        core.videoPayloadTypes.forEach { it.enable(false) }

        core.audioPayloadTypes.forEach {
            it.enable(codecs.contains(Codec.valueOf(it.mimeType.toUpperCase(Locale.ROOT))))
        }

        log("Disabled codecs: " + core.audioPayloadTypes.filter { !it.enabled() }.joinToString(", ") { it.mimeType })
        log("Enabled codecs: " + core.audioPayloadTypes.filter { it.enabled() }.joinToString(", ") { it.mimeType })
    }

    private fun createLinphoneCore(context: Context)
            = Factory.instance().createCore("", "", context)
}

enum class Codec {
    GSM,
    G722,
    G729,
    ILBC,
    ISAC,
    L16,
    OPUS,
    PCMU,
    PCMA,
    SPEEX
}

enum class Port(val value: Int) {
    DISABLED(0), RANDOM(-1)
}

enum class Bandwidth(val value: Int) {
    INFINITE(0)
}

data class Quality(

    /**
     * The average MOS for the entire call.
     */
    val average: Float,

    /**
     * The current MOS at the time requested.
     */
    val current: Float
)


val Call.quality
    get() = Quality(averageQuality, currentQuality)

val Call.displayName
    get() = remoteAddress.displayName ?: ""

val Call.phoneNumber
    get() = remoteAddress.username ?: ""

val Call.wasMissed: Boolean
    get() {
        val log = callLog

        val missedStatuses = arrayOf(
            Call.Status.Missed,
            Call.Status.Aborted,
            Call.Status.EarlyAborted,
        )

        return log.dir == Call.Dir.Incoming && missedStatuses.contains(log.status)
    }

val Call.isOnHold: Boolean
    get() = when (state) {
        Call.State.Paused -> true
        else -> false
    }

internal val Call.identifier: String
    get() = hashCode().toString()