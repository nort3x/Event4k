package com.github.nort3x.event4k

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.internal.AtomicDesc
import kotlinx.coroutines.internal.AtomicOp

@Suppress("UNCHECKED_CAST")
class Event4k : Event4kApi {

    private val scope = CoroutineScope(SupervisorJob())
    private val concurrentBag = Event4kConcurrentBag()
    private var idGen = 0

    override suspend fun <Event> publish(key: String, event: Event): Map<RegisterHook<*>, *> {
        return concurrentBag.computeFromKeyIfExist(key) {
            it.entries.map {
                scope.async {
                    it.key to try {
                        it.value.invoke(event, it.key)
                    }catch (t: Throwable){
                        t
                    }
                }
            }
        }?.awaitAll()?.toMap() ?: emptyMap<RegisterHook<*>, Any?>()
    }
    override suspend fun <Event, OutPut> register(key: String, handler: Handler<Event, OutPut>): RegisterHook<OutPut> {
        return concurrentBag.update(key) {

            val hook = RegisterHook<Any?>(key, idGen++)

            val registration: Handler<Event, OutPut> = { event, registerHook ->
                registerHook.numberOfInvokes++
                registerHook.setLastValue(handler(event, registerHook))
                registerHook.lastValue as OutPut
            }

            hook.deRegisterHook = {
                concurrentBag.removeRegisterHook(key, hook)
            }

            it[hook] = registration as Handler<Any?, Any?>

            it to hook
        } as RegisterHook<OutPut>
    }
}