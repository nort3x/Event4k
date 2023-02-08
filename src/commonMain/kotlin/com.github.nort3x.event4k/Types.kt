package com.github.nort3x.event4k

interface EventHandler<Event, OutPut> {
    suspend fun onEvent(e: Event, registerHook: RegisterHook<OutPut>): OutPut
    suspend fun onError(e: Event, registerHook: RegisterHook<OutPut>, errorEvent: ErrorEvent)
}

class ErrorIgnoringEventHandler<Event, OutPut>(val handler: Handler<Event, OutPut>) : EventHandler<Event, OutPut> {
    override suspend fun onEvent(e: Event, registerHook: RegisterHook<OutPut>): OutPut =
        handler(e, registerHook)

    override suspend fun onError(e: Event, registerHook: RegisterHook<OutPut>, errorEvent: ErrorEvent) {
        // ignore
    }

}

class EventHandlerFromHandlers<Event, OutPut>(
    val handler: Handler<Event, OutPut>,
    val errorHandler: ErrorHandler<Event, OutPut>
) :
    EventHandler<Event, OutPut> {
    override suspend fun onEvent(e: Event, registerHook: RegisterHook<OutPut>): OutPut = handler(e, registerHook)

    override suspend fun onError(e: Event, registerHook: RegisterHook<OutPut>, errorEvent: ErrorEvent): Unit =
        errorHandler(e, registerHook, errorEvent)
}
typealias Handler<Event, Output> = suspend (Event, RegisterHook<Output>) -> Output
typealias ErrorHandler<Event, OutPut> = suspend (Event, RegisterHook<OutPut>, ErrorEvent) -> Unit
typealias ErrorEvent = Pair<Throwable, RegisterHook<*>>
internal typealias BagValue = MutableMap<RegisterHook<Any?>, EventHandler<Any?, Any?>>