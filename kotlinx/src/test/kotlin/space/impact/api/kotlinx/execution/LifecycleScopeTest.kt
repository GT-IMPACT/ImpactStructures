package space.impact.api.kotlinx.execution

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

internal class LifecycleScopeTest {

    @Test
    fun hintBatchCleanupRunsExactlyOnceAfterProjectionFailure() {
        val events: MutableList<String> = mutableListOf()
        val expectedFailure: IllegalStateException = IllegalStateException("projection failed")

        val actualFailure: IllegalStateException = assertThrows(
            IllegalStateException::class.java,
            {
                runLifecycleScope(
                    start = {
                        events.add("start")
                    },
                    finish = {
                        events.add("finish")
                    },
                    block = {
                        events.add("projection")
                        throw expectedFailure
                    },
                )
            },
        )

        assertSame(
            expectedFailure,
            actualFailure,
        )
        assertEquals(
            listOf(
                "start",
                "projection",
                "finish",
            ),
            events,
        )
    }
}
