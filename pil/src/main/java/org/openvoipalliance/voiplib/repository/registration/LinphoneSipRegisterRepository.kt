package org.openvoipalliance.voiplib.repository.registration

import org.linphone.core.*
import org.openvoipalliance.androidphoneintegration.PIL
import org.openvoipalliance.androidphoneintegration.configuration.Auth
import org.openvoipalliance.androidphoneintegration.logWithContext
import org.openvoipalliance.androidphoneintegration.logging.LogLevel
import org.openvoipalliance.androidphoneintegration.logging.LogLevel.ERROR
import org.openvoipalliance.androidphoneintegration.logging.LogLevel.INFO
import org.openvoipalliance.voiplib.RegistrationCallback
import org.openvoipalliance.voiplib.model.RegistrationState.FAILED
import org.openvoipalliance.voiplib.model.RegistrationState.REGISTERED
import org.openvoipalliance.voiplib.repository.LinphoneCoreInstanceManager
import org.openvoipalliance.voiplib.repository.SimpleCoreListener
import java.util.*
import kotlin.concurrent.schedule

internal class LinphoneSipRegisterRepository(
    private val pil: PIL,
    private val linphoneCoreInstanceManager: LinphoneCoreInstanceManager,
) {
    private val registrationListener = RegistrationListener()

    private var callback: RegistrationCallback? = null

    /**
     * We're going to store the auth object that we used to authenticate with successfully, so we
     * know we need to re-register if it has changed.
     */
    private var lastRegisteredCredentials: Auth? = null

    @Throws(CoreException::class)
    fun register(callback: RegistrationCallback) {
        val core = linphoneCoreInstanceManager.safeLinphoneCore ?: run {
            log("Unable to register, no linphone core", ERROR)
            callback(FAILED)
            return
        }

        val auth = pil.auth ?: run {
            log("Unable to register with no auth", ERROR)
            callback(FAILED)
            return
        }

        if (lastRegisteredCredentials != pil.auth && lastRegisteredCredentials != null) {
            log("Auth appears to have changed, unregistering old.")
            unregister()
        }

        core.apply {
            removeListener(registrationListener)
            addListener(registrationListener)
        }

        this.callback = callback

        if (core.accountList.isNotEmpty()) {
            log("SIP account not found, re-registering.")
            core.refreshRegisters()
            return
        }

        log("No SIP account found, registering for the first time.")

        if (auth.port < 1 || auth.port > 65535) {
            throw IllegalArgumentException("Unable to register with a server when port is invalid: ${auth.port}")
        }

        val account = createAccount(core, auth.username, auth.domain, auth.port.toString())

        if (core.addAccount(account) == -1) {
            this.callback = null
            callback(FAILED)
            return
        }

        core.apply {
            addAuthInfo(createAuthInfo(auth))
            defaultAccount = account
        }

        lastRegisteredCredentials = auth
    }

    private fun createAuthInfo(auth: Auth) =
        Factory.instance().createAuthInfo(
            auth.username,
            auth.username,
            auth.password,
            null,
            null,
            auth.domain
        )

    private fun createAccount(
        core: Core,
        name: String,
        domain: String,
        port: String,
    ): Account = core.createAccount(
        core.createAccountParams().apply {
            identityAddress = core.interpretUrl("sip:$name@$domain:$port")
            isRegisterEnabled = true
            // [transport=tls] must be included or you will experience intermittent certification
            // verification issues - especially when changing networks.
            serverAddress = core.interpretUrl("sip:$domain;transport=tls")
        }
    )

    fun unregister() = linphoneCoreInstanceManager.safeLinphoneCore?.apply {
        clearAccounts()
        clearAllAuthInfo()
    }.also {
        log("Unregister complete")
    }

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
            log("State change: ${state?.name} - $message")

            val callback = this@LinphoneSipRegisterRepository.callback ?: run {
                log("Callback not set so registration state change has not done anything.")
                reset()
                return
            }

            // If the registration was successful, just immediately invoke the callback and reset
            // all timers.
            if (state == RegistrationState.Ok) {
                log("Successful, resetting timers.")
                this@LinphoneSipRegisterRepository.callback = null
                callback.invoke(REGISTERED)
                reset()
                return
            }

            // If there is no start time, we want to set it to begin the timer.
            val startTime = this.startTime ?: run {
                val startTime = currentTime
                this.startTime = startTime
                log("Started registration timer: $startTime.")
                startTime
            }

            if (hasExceededTimeout(startTime)) {
                this@LinphoneSipRegisterRepository.callback = null
                unregister()
                log("Registration timeout has been exceeded, registration failed.", ERROR)
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
            startTime = null
            timer?.apply {
                cancel()
                purge()
            }
        }
    }

    private fun log(message: String, level: LogLevel = INFO) =
        logWithContext(message, "SIP-REGISTER", level)

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