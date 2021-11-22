package org.openvoipalliance.voiplib.repository.initialise

import android.content.Context
import org.linphone.core.Factory
import org.openvoipalliance.voiplib.repository.LinphoneCoreInstanceManager
import org.openvoipalliance.voiplib.config.Config as VoIPLibConfig

internal class LinphoneSipInitialiseRepository(private val linphoneCoreInstanceManager: LinphoneCoreInstanceManager, private val context: Context) {

    fun initialise(config: VoIPLibConfig) {
        Factory.instance()
        linphoneCoreInstanceManager.initialiseLinphone(config)
    }

    fun destroy() {
        linphoneCoreInstanceManager.destroy()
    }

    fun swapConfig(config: VoIPLibConfig) {
        linphoneCoreInstanceManager.voipLibConfig = config
    }

    fun refreshRegisters(): Boolean {
        linphoneCoreInstanceManager.safeLinphoneCore?.let {
            it.refreshRegisters()
            return true
        }
        return false
    }

    fun currentConfig(): org.openvoipalliance.voiplib.config.Config = linphoneCoreInstanceManager.voipLibConfig

    fun isInitialised(): Boolean = linphoneCoreInstanceManager.state.initialised

    fun wake() {
        linphoneCoreInstanceManager.safeLinphoneCore?.ensureRegistered()
    }

    fun version(): String = linphoneCoreInstanceManager.safeLinphoneCore?.version ?: ""
}