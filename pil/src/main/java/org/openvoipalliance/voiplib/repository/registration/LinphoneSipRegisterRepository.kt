package org.openvoipalliance.voiplib.repository.registration

import org.linphone.core.*
import org.openvoipalliance.androidphoneintegration.log
import org.openvoipalliance.voiplib.RegistrationCallback
import org.openvoipalliance.voiplib.model.RegistrationState.FAILED
import org.openvoipalliance.voiplib.model.RegistrationState.REGISTERED
import org.openvoipalliance.voiplib.repository.LinphoneCoreInstanceManager
import org.openvoipalliance.voiplib.repository.SimpleCoreListener
import java.util.*
import kotlin.concurrent.schedule

internal class LinphoneSipRegisterRepository(private val linphoneCoreInstanceManager: LinphoneCoreInstanceManager) {

    private val config
        get() = linphoneCoreInstanceManager.voipLibConfig

    private val registrationListener = RegistrationListener()

    private var callback: RegistrationCallback? = null

    @Throws(CoreException::class)
    fun register(callback: RegistrationCallback) {
        val core = linphoneCoreInstanceManager.safeLinphoneCore ?: return

        core.apply {
            removeListener(registrationListener)
            addListener(registrationListener)
        }

        this.callback = callback

        if (core.proxyConfigList.isNotEmpty()) {
            registerLog("Proxy config found, re-registering.")
            core.refreshRegisters()
            return
        }

        registerLog("No proxy config found, registering for the first time.")

        if (config.auth.port < 1 || config.auth.port > 65535) {
            throw IllegalArgumentException("Unable to register with a server when port is invalid: ${config.auth.port}")
        }

        val proxyConfig = createProxyConfig(core, config.auth.name, config.auth.domain, config.auth.port.toString())

        if (core.addProxyConfig(proxyConfig) == -1) {
            callback.invoke(FAILED)
            return
        }

        core.apply {
            addAuthInfo(createAuthInfo())
            defaultProxyConfig = core.proxyConfigList.first()
        }
    }

    private fun createAuthInfo() = Factory.instance().createAuthInfo(config.auth.name, config.auth.name, config.auth.password,
        null, null, "${config.auth.domain}:${config.auth.port}").apply {
        algorithm = null
    }

    private fun createProxyConfig(core: Core, name: String, domain: String, port: String): ProxyConfig {
        val identify = "sip:$name@$domain:$port"
        val proxy = "sip:$domain:$port"
        val identifyAddress = Factory.instance().createAddress(identify)

        return core.createProxyConfig().apply {
            enableRegister(true)
            enableQualityReporting(false)
            qualityReportingCollector = null
            qualityReportingInterval = 0
            identityAddress = identifyAddress
            isPushNotificationAllowed = false
            avpfMode = AVPFMode.Default
            serverAddr = proxy
            natPolicy = null
            done()
        }
    }

    fun unregister() {
        val core = linphoneCoreInstanceManager.safeLinphoneCore ?: return

        core.proxyConfigList.forEach {
            it.edit()
            it.enableRegister(false)
            it.done()
            core.removeProxyConfig(it)
        }

        core.authInfoList.forEach {
            core.removeAuthInfo(it)
        }
    }

    fun isRegistered() = linphoneCoreInstanceManager.state.isRegistered

    private inner class RegistrationListener : SimpleCoreListener {

        /**
         * It is sometimes possible that a failed registration will occur before a successful one
         * so we will track the time of the first registration update before determining it has
         * failed.
         */
        private var startTime: Long? = null

        private val currentTime: Long
            get() = System.currentTimeMillis()

        private var timer: Timer? = null
            set(timer) {
                timer?.cancel()
                timer?.purge()
                field = timer
            }

        override fun onAccountRegistrationStateChanged(
            core: Core,
            account: Account,
            state: RegistrationState?,
            message: String,
        ) {
            registerLog("State change: ${state?.name} - $message")

            val callback = this@LinphoneSipRegisterRepository.callback ?: run {
                registerLog("Callback set so registration state change has not done anything.")
                reset()
                return
            }

            // If the registration was successful, just immediately invoke the callback and reset
            // all timers.
            if (state == RegistrationState.Ok) {
                registerLog("Successful, resetting timers.")
                callback.invoke(REGISTERED)
                reset()
                return
            }

            // If there is no start time, we want to set it to begin the timer.
            val startTime = this.startTime ?: run {
                val startTime = currentTime
                this.startTime = startTime
                registerLog("Started registration timer: $startTime.")
                startTime
            }

            if (hasExceededTimeout(startTime)) {
                unregister()
                registerLog("Registration timeout has been exceeded, registration failed.")
                callback.invoke(FAILED)
                reset()
                return
            }

            // Queuing call of this method so we ensure that the callback is eventually invoked
            // even if there are no future registration updates.
            timer = Timer("Registration").also {
                it.schedule(cleanUpDelay) {
                    onAccountRegistrationStateChanged(
                        core,
                        account,
                        state,
                        "Automatically called to ensure callback is executed"
                    )
                }
            }
        }

        private fun hasExceededTimeout(startTime: Long): Boolean =
            (startTime + registrationTimeoutMs) < currentTime

        private fun reset() {
            this@LinphoneSipRegisterRepository.callback = null
            startTime = null
            timer?.apply {
                cancel()
                purge()
            }
        }
    }

    companion object {
        /**
         * The amount of time to wait before determining registration has failed.
         */
        const val registrationTimeoutMs = 5000L

        /**
         * The time that we will wait before executing the method again to clean-up.
         */
        const val cleanUpDelay = 1000L
    }
}

private fun registerLog(message: String) = log("SIP-REGISTER: $message")