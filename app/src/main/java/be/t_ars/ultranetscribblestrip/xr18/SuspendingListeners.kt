package be.t_ars.ultranetscribblestrip.xr18

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class SuspendingListeners<T> {
	private val listeners = mutableListOf<T>()

	fun add(listener: T) {
		synchronized(listeners) {
			listeners.add(listener)
		}
	}

	fun remove(listener: T) {
		synchronized(listeners) {
			listeners.remove(listener)
		}
	}

	suspend fun broadcast(eventSender: suspend (T) -> Unit) {
        coroutineScope { listeners.map { launch { eventSender(it) } } }
	}
}