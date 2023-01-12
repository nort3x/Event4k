package com.github.nort3x.event4k

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.first

data class RegisterHook<OutPut>(val eventConsumptionKey: String, val registrationId: Int) {

    val regId = eventConsumptionKey to registrationId

    internal lateinit var deRegisterHook: suspend () -> Unit

    var numberOfInvokes: Long = 0
    var lastValue: OutPut? = null
        private set


    fun setLastValue(value: OutPut) {
        this.lastValue = value
        pipe.trySend(RegisterHook<OutPut>(eventConsumptionKey, registrationId).also { other ->
            other.deRegisterHook = deRegisterHook
            other.numberOfInvokes = numberOfInvokes
            other.lastValue = lastValue
        })
    }


    suspend fun deRegister() {
        deRegisterHook()
        isDeRegistered = true
    }

    val isInvokedAtLeastOnce: Boolean
        get() = numberOfInvokes > 0

    var isDeRegistered: Boolean = false
        private set

    private val pipe = Channel<RegisterHook<OutPut>>(capacity = 5, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    suspend fun awaitNextInvoke(): RegisterHook<OutPut> {
        pipe.consumeAsFlow().first()
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as RegisterHook<*>

        if (regId != other.regId) return false

        return true
    }

    override fun hashCode(): Int {
        return regId.hashCode()
    }

    override fun toString(): String =
        "RegisterHook$regId{numberOfInvokes: $numberOfInvokes, lastValue: $lastValue, isDeRegistered: $isDeRegistered, isInvokedAtLeastOnce: $isInvokedAtLeastOnce}"
}