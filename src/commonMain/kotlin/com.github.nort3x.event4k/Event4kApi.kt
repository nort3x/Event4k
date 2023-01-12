package com.github.nort3x.event4k

import kotlin.reflect.KClass

typealias Handler<Event, Output> = suspend (Event, RegisterHook<Output>) -> Output


interface Event4kApi {
    suspend fun <Event> publish(key: String, event: Event): Map<RegisterHook<*>, *>
    suspend fun <Event, OutPut> register(key: String, handler: Handler<Event, OutPut>): RegisterHook<OutPut>

    /**
     * consume a single event and deregister itself, pretty useless,
     * there is no guarantee you receive the last event happening while this is being registered
     */
    suspend fun <Event, Output> consumeOnce(key: String, handler: Handler<Event, Output>): Output {
        val hook = this.register(key) { event: Event, registerHook ->
            val res = handler(event, registerHook)
            registerHook.deRegister()
            res
        }
        return hook.awaitNextInvoke().lastValue as Output
    }
}

fun KClass<*>.extractQualifiedNameOrThrow() = simpleName
    ?: throw IllegalArgumentException("can't extract type qualified name, is it local, nested or anonymous? use direct key overload")

suspend inline fun <reified Event> Event4kApi.publish(event: Event) {
    this.publish(Event::class.extractQualifiedNameOrThrow(), event)
}

suspend inline fun <reified Event, OutPut> Event4kApi.register(noinline handler: Handler<Event, OutPut>): RegisterHook<OutPut> =
    this.register(Event::class.extractQualifiedNameOrThrow(), handler)

/**
 * consume a single event and deregister itself, pretty useless,
 * there is no guarantee you receive the last event happening while this is being registered
 */
suspend inline fun <reified Event, OutPut> Event4kApi.consumeOnce(noinline handler: Handler<Event, OutPut>): OutPut =
    this.consumeOnce(Event::class.extractQualifiedNameOrThrow(), handler)