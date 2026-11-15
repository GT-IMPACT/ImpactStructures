package space.impact.structures.kotlinx.render

import net.minecraft.block.Block
import net.minecraft.block.material.Material
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import space.impact.structures.animation.Animations
import space.impact.structures.animation.RenderTransform
import space.impact.structures.animation.Vector3
import space.impact.structures.kotlinx.animation.spin
import space.impact.structures.render.BlockPosition

internal class RenderModelsTest {

    @Test
    fun standaloneModelKeepsMetadataAndAnimationWithoutHiddenWorldBlocks() {
        val casing = object : Block(Material.iron) {}
        val model = blockModel {
            block(
                x = -1,
                y = 2,
                z = 3,
                block = casing,
                metadata = 7,
            )
        }
        val part = model.animated(spin(degreesPerSecond = 90.0))
        assertSame(
            model,
            part.model,
        )
        assertEquals(
            7,
            model.blocks.getValue(BlockPosition(
                -1,
                2,
                3,
            )).metadata,
        )
        assertTrue(part.hiddenSources.isEmpty())
        val transform = RenderTransform()
        part.animation.apply(
            1.0,
            transform,
        )
        assertEquals(
            -1.0,
            transform.transform(Vector3(
                1.0,
                0.0,
                0.0,
            )).z,
            1e-9,
        )
        assertSame(
            Animations.STILL,
            model.animated().animation,
        )
    }

    @Test
    fun emptyModelStillFailsInsteadOfProducingASilentNoOp() {
        assertThrows(IllegalStateException::class.java) {
            blockModel {}
        }
    }
}
