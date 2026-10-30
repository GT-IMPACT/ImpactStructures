package space.impact.api.kotlinx.execution

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import space.impact.api.ImpactAPI

internal class InstrumentationScopeTest {

    @AfterEach
    fun clearInstrumentation() {
        if (ImpactAPI.isInstrumentEnabled()) {
            ImpactAPI.disableInstrument()
        }
    }

    @Test
    fun returnsTheBlockValueAndClosesTheRealJavaInstrument() {
        val value: String = withInstrumentation(identifier = "normal-return") {
            assertTrue(ImpactAPI.isInstrumentEnabled())
            "value"
        }

        assertEquals(
            "value",
            value,
        )
        assertFalse(ImpactAPI.isInstrumentEnabled())
    }

    @Test
    fun closesTheRealJavaInstrumentAfterAnException() {
        val failure: IllegalArgumentException = assertThrows(
            IllegalArgumentException::class.java,
            {
                withInstrumentation(identifier = "exception") {
                    assertTrue(ImpactAPI.isInstrumentEnabled())
                    throw IllegalArgumentException("body failure")
                }
            },
        )

        assertEquals(
            "body failure",
            failure.message,
        )
        assertFalse(ImpactAPI.isInstrumentEnabled())
    }

    @Test
    fun rejectedNestedScopeLeavesTheOuterInstrumentEnabled() {
        withInstrumentation(identifier = "outer") {
            assertThrows(
                IllegalStateException::class.java,
                {
                    withInstrumentation(identifier = "inner") {
                        throw AssertionError("Nested body must not run")
                    }
                },
            )
            assertTrue(ImpactAPI.isInstrumentEnabled())
        }

        assertFalse(ImpactAPI.isInstrumentEnabled())
    }
}
