package space.impact.api.kotlinx.consumer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import space.impact.api.ImpactStructures
import space.impact.api.kotlinx.execution.withInstrumentation

internal class PublishedConsumerTest {

    @Test
    fun kotlinAndJavaConsumersResolveTheSamePublishedCore() {
        val kotlinDefinition = consumerDefinition()
        val javaDefinition = JavaConsumer.definition()

        assertEquals(
            javaDefinition.getStructureFor("main").size,
            kotlinDefinition.getStructureFor("main").size,
        )
    }

    @Test
    fun scopesWorkFromThePublishedLibraryWithoutReflection() {
        val value = withInstrumentation(identifier = "published-consumer") {
            "completed"
        }

        assertEquals(
            "completed",
            value,
        )
        assertFalse(ImpactStructures.isInstrumentEnabled())
    }
}
