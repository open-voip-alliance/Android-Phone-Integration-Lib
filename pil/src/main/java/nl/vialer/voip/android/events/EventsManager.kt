package nl.vialer.voip.android.events

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import nl.vialer.voip.android.VoIPPIL

class EventsManager(private val pil: VoIPPIL) {

    private var eventListeners = mutableListOf<EventListener>()

    fun listen(listener: EventListener) {
        eventListeners.add(listener)
    }

    fun stopListening(listener: EventListener) {
        eventListeners.remove(listener)
    }

    internal fun broadcast(event: Event) {
        GlobalScope.launch(Dispatchers.Main) {
            pil.writeLog("Broadcasting ${event.name}")
            eventListeners.forEach { it.onEvent(event) }
        }
    }
}