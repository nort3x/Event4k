package com.github.nort3x.event4k

import kotlinx.coroutines.sync.Mutex

typealias BagValue = MutableMap<RegisterHook<Any?>, Handler<Any?, Any?>>
expect class Event4kConcurrentBag constructor() {
    suspend fun <T> computeFromKeyIfExist(key: String, computer: (BagValue) -> T): T?
    suspend fun <T> update(key: String, computer: (BagValue) -> Pair<BagValue,T> ): T

    suspend fun removeRegisterHook(key: String, hook: RegisterHook<Any?>)
}

internal class MultiPlatformEvent4kConcurrentBag{

    private val bag: MutableMap<String, BagValue> = mutableMapOf()
    private val bagMutex = Mutex()

    private suspend fun <T> acquireLockOfBagAndDo(f: () -> T): T {
        bagMutex.lock()
        val res = f()
        bagMutex.unlock()
        return res
    }
    suspend fun <T> computeFromKeyIfExist(key: String, computer: (BagValue) -> T): T?{
        return acquireLockOfBagAndDo {
             bag[key]?.let(computer)
        }
    }
    suspend fun <T> updateAndGet(key: String, computer: (BagValue) -> Pair<BagValue,T> ): T{
        return acquireLockOfBagAndDo {
            val res = computer(bag.getOrPut(key){ mutableMapOf()})
            bag[key] = res.first
            res.second
        }
    }

    suspend fun removeRegisterHook(key: String, hook: RegisterHook<Any?>){
        acquireLockOfBagAndDo {
            bag[key]?.let {
                it.remove(hook)
                if (it.isEmpty())
                    bag.remove(key)
            }
        }
    }
}