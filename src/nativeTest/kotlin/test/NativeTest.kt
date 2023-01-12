package test

import Event4k
import kotlinx.coroutines.runBlocking
import publish
import register
import kotlin.test.Test

class NativeTest {
    @Test
    fun playAround() {
        val event4k = Event4k()
        runBlocking {
            val hook = event4k.register { event: String, _ ->
               kotlin.test.assertEquals(event,"hello!!!")
            }
            event4k.publish("hello!!!")
        }
    }
}