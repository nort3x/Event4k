import com.github.nort3x.event4k.Event4k
import com.github.nort3x.event4k.publish
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class BasicSharedTests {
    private lateinit var event4k: Event4k

    @BeforeTest
    fun makeEvent4k() {
        event4k = Event4k.makeNewInstance()
    }

    @AfterTest
    fun tearDown() = runTest {
        event4k.clear()
    }

    @Test
    fun publishEmptyEventAndExpectEmptyResults() = runTest {
        val invokeResult = event4k.publish("hello")
        assertTrue { invokeResult.isEmpty() }
    }


    @Test
    fun registerOneHandlerAndPublishToIt() = runTest {
        var invoked = false
        val eventToBePublished = "event123"

        val hook = event4k.registerAndIgnoreErrors("key") { event: String, _ ->
            invoked = true
            assertEquals(event, eventToBePublished)
            event + invoked
        }

        val invocationResult = event4k.publish("key", eventToBePublished)


        assertTrue { invoked }
        assertTrue { invocationResult.isNotEmpty() }

        assertEquals(hook.lastValue, eventToBePublished + invoked)
        assertEquals(hook.numberOfInvokes, 1)
        assertTrue { invocationResult.any { it.value == eventToBePublished + invoked } }

    }


    @Test
    fun registerOneHandlerAndPublishToItThenDeregister() = runTest {
        var invoked = false
        val eventToBePublished = "event123"

        val hook = event4k.registerAndIgnoreErrors("key") { event: String, _ ->
            invoked = true
            assertEquals(event, eventToBePublished)
            event + invoked
        }

        val invocationResult = event4k.publish("key", eventToBePublished)


        assertTrue { invoked }
        assertTrue { invocationResult.isNotEmpty() }

        assertEquals(hook.lastValue, eventToBePublished + invoked)
        assertEquals(hook.numberOfInvokes, 1)
        assertTrue { invocationResult.any { it.value == eventToBePublished + invoked } }

        hook.deRegister()

        val invocationResult2 = event4k.publish("key", eventToBePublished)
        assertTrue { invocationResult2.isEmpty() }

        assertEquals(hook.lastValue, eventToBePublished + invoked)
        assertEquals(hook.numberOfInvokes, 1)
    }

    @Test
    fun consumeOneEvent() = runTest {
        var invoked = false
        val eventToBePublished = "event123"

        val result = async {
            event4k.consumeOnceAndIgnoreErrors("key") { event: String, _ ->
                invoked = true
                assertEquals(event, eventToBePublished)
                event + invoked
            }
        }

        delay(10)
        val invocationResult = event4k.publish("key", eventToBePublished)
        result.join()


        assertTrue { invoked }
        assertTrue { invocationResult.isNotEmpty() }
        assertTrue { invocationResult.any { it.value == eventToBePublished + invoked } }
        assertEquals(result.await(), eventToBePublished + invoked)

        val invocationResult2 = event4k.publish("key", eventToBePublished)
        assertTrue { invocationResult2.isEmpty() }

    }
}