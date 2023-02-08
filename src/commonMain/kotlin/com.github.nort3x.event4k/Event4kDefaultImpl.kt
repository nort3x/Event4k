package com.github.nort3x.event4k

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

@Suppress("UNCHECKED_CAST")
internal class Event4kDefaultImpl : Event4k {

    private val scope = CoroutineScope(SupervisorJob())
    private val concurrentBag = Event4kConcurrentBag()
    private var idGen = 0

    override suspend fun <Event> publish(key: String, event: Event): Map<RegisterHook<*>, *> {
        var error: ErrorEvent? = null
        val listeners = concurrentBag.computeFromKeyIfExist(key) { it.entries.toList() }
            ?: return emptyMap<RegisterHook<Any?>, Any?>() as Map<RegisterHook<*>, *>

        val invokeResult = listeners.map { entry ->
            scope.async {
                entry.key to try {
                    entry.value.onEvent(event, entry.key)
                } catch (t: Throwable) {
                    error = t to entry.key
                    t
                }
            }
        }.awaitAll().toMap()

        if(error == null)
            return invokeResult as Map<RegisterHook<*>, *>

        listeners.map {
            scope.async {
                it.value.onError(event,it.key,error!!)
            }
        }.awaitAll()

        throw error!!.first

    }

    override suspend fun <Event, OutPut> register(
        key: String,
        handler: EventHandler<Event, OutPut>
    ): RegisterHook<OutPut> {
        return concurrentBag.update(key) {

            val hook = RegisterHook<Any?>(key, idGen++)

            val registration: EventHandler<Event, OutPut> = EventHandlerFromHandlers(
                { event, registerHook ->
                    registerHook.numberOfInvokes++
                    registerHook.setLastValue(handler.onEvent(event, registerHook))
                    registerHook.lastValue as OutPut
                }
            ) { e, registerHook, errorEvent -> handler.onError(e, registerHook, errorEvent) }

            hook.deRegisterHook = {
                concurrentBag.removeRegisterHook(key, hook)
            }

            it[hook] = registration as EventHandler<Any?, Any?>

            it to hook
        } as RegisterHook<OutPut>
    }

    override suspend fun clear() {
        concurrentBag.removeAll()
    }
}