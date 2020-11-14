package nl.vialer.voip.android.configuration

import nl.vialer.voip.android.events.EventListener
import nl.vialer.voip.android.logging.LogCallback

data class Configuration(
    val auth: Auth,
    val eventListener: EventListener? = null,
    val log: LogCallback? = null,

)