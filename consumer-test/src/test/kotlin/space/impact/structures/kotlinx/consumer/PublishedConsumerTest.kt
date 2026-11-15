package space.impact.structures.kotlinx.consumer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import space.impact.structures.ImpactStructures
import space.impact.structures.kotlinx.execution.withInstrumentation
import space.impact.structures.animation.RenderTransform
import space.impact.structures.animation.Vector3
import space.impact.structures.structure.IStructureDefinition

internal class PublishedConsumerTest {

    @Test
    fun animationDslIsAvailableFromTheSamePublishedArtifact() {
        val transform = RenderTransform()
        consumerAnimation().apply(
            2.0,
            transform,
        )
        assertEquals(
            6.0,
            transform.transform(Vector3.ZERO).y,
            1e-9,
        )
        assertEquals(
            IStructureDefinition::class.java.protectionDomain.codeSource.location,
            Class.forName("space.impact.structures.kotlinx.animation.AnimationsKt").protectionDomain.codeSource.location,
        )
    }

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
