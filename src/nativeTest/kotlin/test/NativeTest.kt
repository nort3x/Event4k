package test

import com.github.nort3x.event4k.*
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class NativeTest {
    @Test
    fun playAround() {
        val event4k = Event4k()
        runBlocking {
            event4k.register { event: String, _ ->
               kotlin.test.assertEquals(event,"hello!!!")
            }
            event4k.publish("hello!!!")
        }
    }
}