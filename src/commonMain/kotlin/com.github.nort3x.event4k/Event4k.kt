package com.github.nort3x.event4k

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Mutex

class Event4k : Event4kApi {

    private val scope = CoroutineScope(SupervisorJob())

    private val bag: MutableMap<String, MutableMap<RegisterHook<Any?>, Handler<Any?, Any?>>> = mutableMapOf()
    private val bagMutex = Mutex()

    private suspend fun <T> acquireLockOfBagAndDo(f: () -> T): T {
        bagMutex.lock()
        val res = f()
        bagMutex.unlock()
        return res
    }

    override suspend fun <Event> publish(key: String, event: Event): Map<RegisterHook<*>, *> {
        return acquireLockOfBagAndDo {
            bag[key]?.let {
                it.entries.map {
                    scope.async {
                        it.key to it.value(event, it.key)
                    }
                }
            }
        }?.awaitAll()?.toMap() ?: emptyMap<RegisterHook<*>, Any?>()
    }

    private var idGen = 0
    override suspend fun <Event, OutPut> register(key: String, handler: Handler<Event, OutPut>): RegisterHook<OutPut> {
        return acquireLockOfBagAndDo {
            val subBag = bag.getOrPut(key) { mutableMapOf() }

            val hook = RegisterHook<Any?>(key, idGen++)

            val registration: Handler<Event, OutPut> = { event, registerHook ->
                registerHook.numberOfInvokes++
                registerHook.setLastValue(handler(event, registerHook))
                registerHook.lastValue as OutPut
            }

            hook.deRegisterHook = {
                acquireLockOfBagAndDo {
                    bag[key]?.let {
                        it.remove(hook)
                        if (it.isEmpty())
                            bag.remove(key)
                    }
                }
            }

            subBag[hook] = registration as Handler<Any?, Any?>

            hook
        } as RegisterHook<OutPut>
    }
}