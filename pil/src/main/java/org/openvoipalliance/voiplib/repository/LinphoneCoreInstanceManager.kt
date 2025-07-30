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
import org.openvoipalliance.androidphoneintegration.PIL
import org.openvoipalliance.androidphoneintegration.R
import org.openvoipalliance.voiplib.config.Config
import org.openvoipalliance.voiplib.model.Call
import org.openvoipalliance.voiplib.model.Codec
import org.openvoipalliance.voiplib.repository.initialize.LogLevel
import java.io.File
import java.io.FileOutputStream
import java.util.*
import org.linphone.core.Call as LinphoneCall

internal class LinphoneCoreInstanceManager(private val context: Context): SimpleCoreListener, LoggingServiceListener {

    internal val state = CoreState()

    private lateinit var voipLibConfig: Config

    private var linphoneCore: Core? = null

    internal val logging: LoggingService
        get() = Factory.instance().loggingService

    val safeLinphoneCore: Core?
        get() {
            return if (state.initialized) {
                linphoneCore
            } else {
                Log.e(TAG, "Trying to get linphone core while not possible", Exception())
                null
            }
        }

    /**
     * Certain files need to be available to Linphone via the file system, rather than through a
     * native resource. Each time we initialize Linphone we will copy the resource to the file
     * location listed.
     */
    private val filesToPublish = mapOf(
        R.raw.ringback to "ringback.wav"
    )

    fun initializeLinphone(config: Config) {
        this.voipLibConfig = config

        if (linphoneCore != null) {
            config.logListener?.onLogMessageWritten(LogLevel.WARNING, "Not initializing linphone, already initialized.")
            return
        }

        try {
            publishResources()
            startLibLinphone()
        } catch (e: Exception) {
            config.logListener?.onLogMessageWritten(LogLevel.ERROR, "Failed to start Linphone: ${e.localizedMessage}")
            Log.e(TAG, "startLibLinphone: cannot start linphone")
        }
    }

    @Synchronized
    @Throws(Exception::class)
    private fun startLibLinphone() {
        voipLibConfig.logListener.let { logging.addListener(this) }

        this.linphoneCore = createLinphoneCore(context).also {
            applyPreStartConfiguration(it)
            it.start()
            applyPostStartConfiguration(it)
            configureCodecs(it)
            log("Started Linphone with config:\n ${it.config.dump()}")
        }

        state.destroyed = false
    }

    fun setLoggingLevel(enableAdvancedLogging: Boolean) = logging.setLogLevel(
        when(enableAdvancedLogging) {
            true -> Debug
            false -> Warning
        })

    private fun applyPreStartConfiguration(core:Core) = core.apply {
        addListener(this@LinphoneCoreInstanceManager)
        pauseCallsWhenAudioFocusLost = false
        disableAudioFocusRequests = true
        automaticNetworkStateMonitoring = true
        registerOnlyWhenNetworkIsUp = true
        isPushNotificationEnabled = false
        isDnsSearchEnabled = false
        mediaEncryption = MediaEncryption.SRTP
        isMediaEncryptionMandatory = true
        setUserAgent(voipLibConfig.userAgent, null)
        maxCalls = 2
        ring = null
        ringback = publishedFile("ringback.wav").absolutePath
        isNativeRingingEnabled = false
        isVideoCaptureEnabled = false
        isVideoDisplayEnabled = false
        isAutoIterateEnabled = true
        isIpv6Enabled = false
        stunServer = voipLibConfig.stun
        mtu = 1300
        guessHostname = true
        autoAnswerReplacingCalls = true
        pingWithOptions = false
        natPolicy = natPolicy?.apply {
            isStunEnabled = voipLibConfig.stun?.isNotEmpty() == true
        }
        // keepAlive MUST be set to FALSE otherwise there are issues with calls that aren't
        // immediately answered. Test this fully if you change it.
        isKeepAliveEnabled = false
    }

    private fun applyPostStartConfiguration(core: Core) = core.apply {
        useInfoForDtmf = true
        isAdaptiveRateControlEnabled = true

        if (hasBuiltinEchoCanceller()) {
            isEchoCancellationEnabled = false
            log("Built-in echo cancellation detected, disabling software.")
        } else {
            isEchoCancellationEnabled = true
            log("This device does not have built-in echo cancellation, enabled software.")
        }
    }

    /**
     * Iterate over the [filesToPublish] and publish any that are necessary into the appropriate
     * file system directory.
     *
     * @param overwrite
     */
    private fun publishResources(overwrite: Boolean = true) {
        filesToPublish.forEach { (id, filename) ->
            log("Publishing resource $id to $filename")

            val publishedFile = publishedFile(filename)

            val parent = publishedFile.parentFile ?: return

            if (!parent.exists()) {
                parent.mkdir()
            }

            when {
                publishedFile.exists() && overwrite -> publishedFile.delete()
                publishedFile.exists() && !overwrite -> return
            }

            val outStream = FileOutputStream(publishedFile)
            val inStream = context.resources.openRawResource(id)
            val buffer = ByteArray(1024)
            var length: Int = inStream.read(buffer)

            while (length > 0) {
                outStream.write(buffer, 0, length)
                length = inStream.read(buffer)
            }

            inStream.close()
            outStream.flush()
            outStream.close()
        }
    }

    /**
     * Get the full file object for a published filename.
     */
    private fun publishedFile(filename: String) =
        File("${context.filesDir}/${PUBLISH_DIRECTORY_NAME}/$filename")

    /**
     * Creates the Linphone core by reading in the linphone raw configuration file.
     *
     */
    private fun createLinphoneCore(context: Context) =
        Factory.instance().createCore("", "", context)

    internal fun log(message: String, level: LogLevel = LogLevel.DEBUG) {
        voipLibConfig.logListener?.onLogMessageWritten(message = message, lev = level)
    }

    private fun configureCodecs(core: Core) {
        val codecs = this.voipLibConfig.codecs

        core.videoPayloadTypes.forEach { it.enable(false) }

        core.audioPayloadTypes.forEach {
            it.enable(codecs.contains(Codec.valueOf(it.mimeType.uppercase(Locale.ROOT))))
        }

        log("Disabled codecs: " + core.audioPayloadTypes.filter { !it.enabled() }.joinToString(", ") { it.mimeType })
        log("Enabled codecs: " + core.audioPayloadTypes.filter { it.enabled() }.joinToString(", ") { it.mimeType })
    }

    private var previousState = LinphoneCall.State.Idle

    override fun onCallStateChanged(lc: Core, linphoneCall: LinphoneCall, state: LinphoneCall.State, message: String) {
        log("callState: $state, Message: $message, Duration = ${linphoneCall.duration}")

        preserveInviteData(linphoneCall)

        val call = Call(linphoneCall)

        when (state) {
            LinphoneCall.State.IncomingReceived -> voipLibConfig.callListener.incomingCallReceived(call)
            LinphoneCall.State.OutgoingInit -> voipLibConfig.callListener.outgoingCallCreated(call)
            LinphoneCall.State.StreamsRunning -> {
                if (previousState == LinphoneCall.State.Connected) {
                    voipLibConfig.callListener.streamsStarted()
                }
            }
            LinphoneCall.State.Connected -> {
                safeLinphoneCore?.activateAudioSession(true)
                voipLibConfig.callListener.callConnected(call)
            }
            LinphoneCall.State.End, LinphoneCall.State.Error -> voipLibConfig.callListener.callEnded(call)
            LinphoneCall.State.Released -> voipLibConfig.callListener.callReleased(call)
            else -> voipLibConfig.callListener.callUpdated(call)
        }

        previousState = state
    }

    override fun onAudioDeviceChanged(core: Core, audioDevice: AudioDevice) =
        voipLibConfig.callListener.currentAudioDeviceHasChanged(audioDevice)

    override fun onAudioDevicesListUpdated(core: Core) =
        voipLibConfig.callListener.availableAudioDevicesUpdated()

    override fun onRemainingNumberOfFileTransferChanged(
        core: Core,
        downloadCount: Int,
        uploadCount: Int
    ) {
        log("onRemainingNumberOfFileTransferChanged: Not implemented")
    }

    override fun onMessageWaitingIndicationChanged(
        core: Core,
        lev: Event,
        mwi: MessageWaitingIndication
    ) {
        log("onMessageWaitingIndicationChanged: Not implemented")
    }

    override fun onSnapshotTaken(core: Core, filePath: String) {
        log("onSnapshotTaken: Not implemented")
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

    override fun onReferReceived(
        core: Core,
        referToAddr: Address,
        customHeaders: Headers,
        content: Content?
    ) {
        TODO("Not yet implemented")
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

    companion object {
        const val TAG = "VOIPLIB-LINPHONE"
        var globalState: GlobalState = Off
        const val PUBLISH_DIRECTORY_NAME = "apl-resources"
    }

    inner class CoreState {
        var destroyed: Boolean = false
        val initialized: Boolean get() = linphoneCore != null && !destroyed
    }
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