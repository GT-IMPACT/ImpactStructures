package space.impact.structures.kotlinx.execution

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import space.impact.structures.ImpactStructures

internal class InstrumentationScopeTest {

    @AfterEach
    fun clearInstrumentation() {
        if (ImpactStructures.isInstrumentEnabled()) {
            ImpactStructures.disableInstrument()
        }
    }

    @Test
    fun returnsTheBlockValueAndClosesTheRealJavaInstrument() {
        val value: String = withInstrumentation(identifier = "normal-return") {
            assertTrue(ImpactStructures.isInstrumentEnabled())
            "value"
        }

        assertEquals(
            "value",
            value,
        )
        assertFalse(ImpactStructures.isInstrumentEnabled())
    }

    @Test
    fun closesTheRealJavaInstrumentAfterAnException() {
        val failure: IllegalArgumentException = assertThrows(
            IllegalArgumentException::class.java,
            {
                withInstrumentation(identifier = "exception") {
                    assertTrue(ImpactStructures.isInstrumentEnabled())
                    throw IllegalArgumentException("body failure")
                }
            },
        )

        assertEquals(
            "body failure",
            failure.message,
        )
        assertFalse(ImpactStructures.isInstrumentEnabled())
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
            assertTrue(ImpactStructures.isInstrumentEnabled())
        }

        assertFalse(ImpactStructures.isInstrumentEnabled())
    }
}
