package org.openvoipalliance.voiplib

import android.Manifest.permission.RECORD_AUDIO
import androidx.annotation.RequiresPermission
import org.linphone.core.Factory
import org.openvoipalliance.androidphoneintegration.di.di
import org.openvoipalliance.voiplib.config.Config
import org.openvoipalliance.voiplib.model.Call
import org.openvoipalliance.voiplib.model.RegistrationState
import org.openvoipalliance.voiplib.repository.LinphoneCoreInstanceManager
import org.openvoipalliance.voiplib.repository.call.controls.LinphoneSipActiveCallControlsRepository
import org.openvoipalliance.voiplib.repository.call.session.LinphoneSipSessionRepository
import org.openvoipalliance.voiplib.repository.registration.LinphoneSipRegisterRepository

typealias RegistrationCallback = (RegistrationState) -> Unit

class VoIPLib {
    private val sipRegisterRepository: LinphoneSipRegisterRepository by di.koin.inject()
    private val sipCallControlsRepository: LinphoneSipActiveCallControlsRepository by di.koin.inject()
    private val sipSessionRepository: LinphoneSipSessionRepository by di.koin.inject()
    private val linphoneCoreInstanceManager: LinphoneCoreInstanceManager by di.koin.inject()

    /**
     * This needs to be called whenever this library needs to initialise. Without it, no other calls
     * can be done.
     */
    fun initialise(config: Config): VoIPLib {
        Factory.instance()
        linphoneCoreInstanceManager.initialiseLinphone(config)
        return this
    }

    /**
     * Check to see if the phonelib is initialised and ready to make calls.
     *
     */
    val isInitialised: Boolean
        get() = linphoneCoreInstanceManager.state.initialised

    val isNetworkReachable
        get() = linphoneCoreInstanceManager.safeLinphoneCore?.isNetworkReachable ?: false

    /**
     * This registers your user on SIP. You need this before placing a call.
     *
     */
    fun register(callback: RegistrationCallback): VoIPLib {
        sipRegisterRepository.register(callback)
        return this
    }

    /**
     * This unregisters your user on SIP.
     */
    fun unregister() = sipRegisterRepository.unregister()

    /**
     * This method audio calls a phone number
     * @param number the number dialed to
     * @return returns true when call succeeds, false when the number is an empty string or the
     * phone service isn't ready.
     */
    @RequiresPermission(RECORD_AUDIO)
    fun callTo(number: String) = sipSessionRepository.callTo(number)

    /**
     * A method that should be called when the application has received a push message and
     * is waking from the background.
     *
     */
    fun wake() = linphoneCoreInstanceManager.safeLinphoneCore?.ensureRegistered()

    /**
     * Whether or not the microphone is currently muted.
     *
     * Set to TRUE to mute the microphone and prevent voice transmission.
     */
    var microphoneMuted
            get() = sipCallControlsRepository.isMicrophoneMuted()
            set(muted) = sipCallControlsRepository.setMicrophone(!muted)

    /**
     * Perform actions on the given call.
     *
     */
    fun actions(call: Call) = Actions(call)

    /**
     * Return the current version of the underlying voip library.
     *
     */
    fun version() = linphoneCoreInstanceManager.safeLinphoneCore?.version ?: ""

    fun refreshRegistration() {
        linphoneCoreInstanceManager.safeLinphoneCore?.refreshRegisters()
    }
}