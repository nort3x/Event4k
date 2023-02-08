package com.github.nort3x.event4k

actual class Event4kConcurrentBag {
    private val multiPlatformEvent4kConcurrentBag = MultiPlatformEvent4kMutexLockingConcurrentBag()
    actual suspend fun <T> computeFromKeyIfExist(key: String, computer: (BagValue) -> T): T? =
        multiPlatformEvent4kConcurrentBag.computeFromKeyIfExist(key, computer)

    actual suspend fun <T> update(key: String, computer: (BagValue) -> Pair<BagValue, T>): T =
        multiPlatformEvent4kConcurrentBag.updateAndGet(key, computer)

    actual suspend fun removeRegisterHook(key: String, hook: RegisterHook<Any?>) =
        multiPlatformEvent4kConcurrentBag.removeRegisterHook(key, hook)


    actual suspend fun removeAll() =
        multiPlatformEvent4kConcurrentBag.removeAll()

}