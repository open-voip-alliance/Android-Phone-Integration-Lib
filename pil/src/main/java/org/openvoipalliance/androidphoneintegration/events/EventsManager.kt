package org.openvoipalliance.androidphoneintegration.events

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.openvoipalliance.androidphoneintegration.PIL
import org.openvoipalliance.androidphoneintegration.events.Event.CallSessionEvent.*
import org.openvoipalliance.androidphoneintegration.events.Event.CallSessionEvent.AttendedTransferEvent.*
import kotlin.reflect.KClass

class EventsManager internal constructor(private val pil: PIL) {

    private var eventListeners = mutableListOf<PILEventListener>()

    fun listen(listener: PILEventListener) {
        if (eventListeners.contains(listener)) return

        eventListeners.add(listener)
    }

    fun stopListening(listener: PILEventListener) {
        eventListeners.remove(listener)
    }

    internal fun broadcast(event: Event) = GlobalScope.launch(Dispatchers.Main) {
        if (!pil.isStarted) return@launch

        eventListeners.forEach {
            it.onEvent(event)
        }
    }

    internal fun broadcast(eventClass: KClass<out Event.CallSessionEvent>) {
        val state = pil.sessionState

        broadcast(
            when(eventClass) {
                OutgoingCallStarted::class -> OutgoingCallStarted(state)
                IncomingCallReceived::class -> IncomingCallReceived(state)
                CallEnded::class -> CallEnded(state)
                CallConnected::class -> CallConnected(state)
                AttendedTransferStarted::class -> AttendedTransferStarted(state)
                AttendedTransferConnected::class -> AttendedTransferConnected(state)
                AttendedTransferAborted::class -> AttendedTransferAborted(state)
                AttendedTransferEnded::class -> AttendedTransferEnded(state)
                AudioStateUpdated::class -> AudioStateUpdated(state)
                CallDurationUpdated::class -> CallDurationUpdated(state)
                CallStateUpdated::class -> CallStateUpdated(state)
                else -> throw IllegalArgumentException("Attempted to broadcast an unregistered event (${eventClass.qualifiedName}), make sure to register in EventsManager")
            }
        )
    }
}
