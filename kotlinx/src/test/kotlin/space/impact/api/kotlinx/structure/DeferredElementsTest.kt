package space.impact.api.kotlinx.structure

import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import space.impact.api.structure.IStructureElement

internal class DeferredElementsTest {

    @Test
    fun lazyCachesFirstFactoryWhileDeferredResolvesEachCall() {
        val world = StructureTestWorld.create(object : Block(Material.rock) {})
        val accepted = element(true)
        var lazyCalls = 0
        var deferredCalls = 0
        val cached = lazyElement<StructureController> {
            lazyCalls += 1
            accepted
        }
        val dynamic = deferredElementFor<StructureController> {
            deferredCalls += 1
            accepted
        }
        repeat(3) {
            cached.check(
                StructureController(),
                world,
                0,
                0,
                0,
            )
            dynamic.check(
                StructureController(),
                world,
                0,
                0,
                0,
            )
        }
        assertEquals(
            1,
            lazyCalls,
        )
        assertEquals(
            3,
            deferredCalls,
        )
    }

    @Test
    fun checkAndTriggerSelectorsRemainIndependentAndSupportNullTrigger() {
        val world = StructureTestWorld.create(object : Block(Material.rock) {})
        val controller = StructureController()
        val accepted = element(true)
        val rejected = element(false)
        val partitioned = partitionByCheckAndTrigger(
            elements = mapOf(
                0 to accepted,
                1 to rejected,
            ),
            checkKey = StructureController::selected,
            triggerKey = { trigger ->
                trigger?.stackSize ?: selected
            },
            fallback = rejected,
        )
        assertTrue(partitioned.check(
            controller,
            world,
            0,
            0,
            0,
        ))
        assertFalse(partitioned.spawnHint(
            controller,
            world,
            0,
            0,
            0,
            ItemStack(Item()),
        ))
        assertTrue(partitioned.spawnHint(
            controller,
            world,
            0,
            0,
            0,
            null,
        ))
        controller.selected = 9
        assertFalse(partitioned.check(
            controller,
            world,
            0,
            0,
            0,
        ))
    }

    @Test
    fun varargIndexPartitionAndCallbacksPreserveShortCircuiting() {
        val world = StructureTestWorld.create(object : Block(Material.rock) {})
        val controller = StructureController()
        val accepted = element(true)
        val rejected = element(false)
        val selected = partitionByIndex(
            elements = arrayOf(
                rejected,
                accepted,
            ),
            index = {
                selected
            },
        )
        controller.selected = 1
        val observed = onPass(
            element = selected,
            action = {
                callbacks += 1
            },
        )
        val chained = chain(
            observed,
            onPass(
                element = accepted,
                action = {
                    callbacks += 100
                },
            ),
        )
        assertTrue(chained.check(
            controller,
            world,
            0,
            0,
            0,
        ))
        assertEquals(
            1,
            controller.callbacks,
        )
    }

    private fun element(accepted: Boolean): IStructureElement<StructureController> {
        return customElement(
            check = { _, _, _, _ ->
                accepted
            },
            hint = { _, _, _, _, _ ->
                accepted
            },
        )
    }
}
