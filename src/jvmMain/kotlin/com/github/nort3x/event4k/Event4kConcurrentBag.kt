package com.github.nort3x.event4k

import java.util.concurrent.ConcurrentHashMap

actual class Event4kConcurrentBag {

    private val bag = ConcurrentHashMap<String, BagValue>()
    actual suspend fun <T> computeFromKeyIfExist(key: String, computer: (BagValue) -> T): T? {
        var result: T? = null
        bag.computeIfPresent(key) { _, v ->
            result = computer(v)
            v
        }
        return result
    }

    actual suspend fun <T> update(key: String, computer: (BagValue) -> Pair<BagValue, T>): T {
        var ret: T? = null
        bag.compute(key) { k, v ->
            val actualBagValue = v ?: mutableMapOf()
            val res = computer(actualBagValue)
            ret = res.second
            res.first
        }
        return ret!!
    }

    actual suspend fun removeRegisterHook(key: String, hook: RegisterHook<Any?>) {
        bag.computeIfPresent(key) { k, v ->
            v.remove(hook)
            if (v.isEmpty())
                null
            else v
        }
    }

}