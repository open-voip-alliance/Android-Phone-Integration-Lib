package org.openvoipalliance.androidphoneintegration.helpers

import org.openvoipalliance.androidphoneintegration.PIL
import org.openvoipalliance.androidphoneintegration.isNullOrInvalid
import org.openvoipalliance.androidphoneintegration.logWithContext
import org.openvoipalliance.androidphoneintegration.logging.LogLevel.ERROR
import org.openvoipalliance.voiplib.VoIPLib
import org.openvoipalliance.voiplib.model.RegistrationState.FAILED
import org.openvoipalliance.voiplib.model.RegistrationState.REGISTERED
import org.openvoipalliance.androidphoneintegration.logging.LogLevel as PilLogLevel

internal class VoIPLibHelper(
    private val pil: PIL,
    private val phoneLib: VoIPLib,
) {

    /**
     * Attempt to register if there are valid credentials.
     */
    fun register(callback: (Boolean) -> Unit) {
        if (pil.auth.isNullOrInvalid) {
            log("Unable to register as we have no authentication credentials", ERROR)
            callback.invoke(false)
            return
        }

        pil.writeLog("Attempting registration...")

        phoneLib.register { registrationState ->
            when (registrationState) {
                REGISTERED -> {
                    log("Registration was successful!")
                    callback.invoke(true)
                }
                FAILED -> {
                    log("Unable to register...")
                    callback.invoke(false)
                }
                else -> {}
            }
        }
    }

    private fun log(message: String, level: PilLogLevel = PilLogLevel.INFO) =
        logWithContext(message, "PHONE-LIB-HELPER", level)
}
