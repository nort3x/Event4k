import com.github.nort3x.event4k.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.test.BeforeTest


class Test {

    private lateinit var event4k: Event4kApi

    @BeforeTest
    fun refreshEnv() {
        event4k = Event4k()
    }


    @Test
    fun `Basic Register Publish Test`() = runBlocking {

        var inv: Long = 0
        val w = event4k.register("key") { event: String, registerHook ->
            Assertions.assertEquals(++inv, registerHook.numberOfInvokes)
            inv
        }

        repeat(100) {
            val pub = event4k.publish("key", "value")
            Assertions.assertTrue(pub.isNotEmpty())
            Assertions.assertTrue(pub.entries.first().key === w)
            Assertions.assertEquals(pub.entries.first().value, inv)
        }

        Assertions.assertEquals(w.numberOfInvokes, inv)
        Assertions.assertEquals(w.lastValue, inv)
        Assertions.assertTrue(w.isInvokedAtLeastOnce)

        println(w)

    }

    @Test
    fun `Consume Once Test`() = runBlocking {

        var stealHook: RegisterHook<String>? = null

        val j = launch {
            event4k.consumeOnce { event: String, hook: RegisterHook<String> ->
                Assertions.assertEquals(event, "event")
                stealHook = hook
                "hi"
            }
        }
        delay(10)

        val res = event4k.publish("event")
        j.join()
        Assertions.assertTrue(j.isCompleted)

        Assertions.assertEquals(stealHook?.lastValue!!, "hi")

    }
}