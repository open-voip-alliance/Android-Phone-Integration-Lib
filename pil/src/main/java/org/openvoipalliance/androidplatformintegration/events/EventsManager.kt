package org.openvoipalliance.androidplatformintegration.events

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.openvoipalliance.androidplatformintegration.PIL

class EventsManager internal constructor(private val pil: PIL) {

    private var eventListeners = mutableListOf<PILEventListener>()

    fun listen(listener: PILEventListener) {
        eventListeners.add(listener)
    }

    fun stopListening(listener: PILEventListener) {
        eventListeners.remove(listener)
    }

    internal fun broadcast(event: Event) {
        GlobalScope.launch(Dispatchers.Main) {
            pil.writeLog("Broadcasting ${event::javaClass.name}")
            eventListeners.forEach { it.onEvent(event) }
        }
    }
}
