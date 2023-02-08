package com.github.nort3x.event4k

import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
interface Event4k {
    suspend fun <Event> publish(key: String, event: Event): Map<RegisterHook<*>, *>
    suspend fun <Event, OutPut> register(key: String, handler: EventHandler<Event, OutPut>): RegisterHook<OutPut>

    suspend fun clear()
    suspend fun <Event, OutPut> registerAndIgnoreErrors(
        key: String,
        handler: Handler<Event, OutPut>
    ): RegisterHook<OutPut> =
        register(key, ErrorIgnoringEventHandler(handler))


    /**
     * consume a single event and deregister itself, pretty useless,
     * there is no guarantee you receive the last event happening while this is being registered
     */
    suspend fun <Event, OutPut> consumeOnce(key: String, handler: EventHandler<Event, OutPut>): OutPut {
        val hook = this.register(key, object : EventHandler<Event, OutPut> {
            override suspend fun onEvent(e: Event, registerHook: RegisterHook<OutPut>): OutPut {
                val res = handler.onEvent(e, registerHook)
                registerHook.deRegister()
                return res
            }

            override suspend fun onError(e: Event, registerHook: RegisterHook<OutPut>, errorEvent: ErrorEvent) {
                handler.onError(e, registerHook, errorEvent)
            }
        })
        return hook.awaitNextInvoke().lastValue as OutPut
    }

    suspend fun <Event, OutPut> consumeOnceAndIgnoreErrors(key: String, handler: Handler<Event, OutPut>): OutPut =
        consumeOnce(key, ErrorIgnoringEventHandler(handler))

    companion object {
        val default: Event4k by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { Event4kDefaultImpl() }
        fun makeNewInstance(): Event4k = Event4kDefaultImpl()
    }
}

fun KClass<*>.extractQualifiedNameOrThrow() = simpleName
    ?: throw IllegalArgumentException("can't extract type qualified name, is it local, nested or anonymous? use direct key overload")

suspend inline fun <reified Event> Event4k.publish(event: Event) =
    this.publish(Event::class.extractQualifiedNameOrThrow(), event)

suspend inline fun <reified Event, OutPut> Event4k.register(handler: EventHandler<Event, OutPut>): RegisterHook<OutPut> =
    this.register(Event::class.extractQualifiedNameOrThrow(), handler)
suspend inline fun <reified Event, OutPut> Event4k.registerAndIgnoreErrors(noinline handler: Handler<Event, OutPut>): RegisterHook<OutPut> =
    this.registerAndIgnoreErrors(Event::class.extractQualifiedNameOrThrow(),handler)

/**
 * consume a single event and deregister itself, pretty useless,
 * there is no guarantee you receive the last event happening while this is being registered
 */
suspend inline fun <reified Event, OutPut> Event4k.consumeOnce(handler: EventHandler<Event, OutPut>): OutPut =
    this.consumeOnce(Event::class.extractQualifiedNameOrThrow(), handler)

suspend inline fun <reified Event, OutPut> Event4k.consumeOnceAndIgnoreErrors(noinline handler: Handler<Event, OutPut>): OutPut =
    this.consumeOnceAndIgnoreErrors(Event::class.extractQualifiedNameOrThrow(), handler)