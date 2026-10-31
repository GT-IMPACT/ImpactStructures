package space.impact.structures.kotlinx.inventory

import net.minecraft.inventory.InventoryBasic
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import space.impact.structures.structure.IItemSource

internal class InventoryExtensionsTest {

    @Test
    internal fun simulationAndInsufficientRequestsDoNotConsumeItems() {
        val item: Item = Item()
        val inventory: InventoryBasic = InventoryBasic(
            "KotlinX inventory",
            true,
            2,
        )
        inventory.setInventorySlotContents(
            0,
            ItemStack(
                item,
                7,
                0,
            ),
        )
        val source: IItemSource = inventory.itemSource()

        val simulated: ItemStack? = source.takeOne(
            simulate = true,
        ) { stack ->
            stack.item === item
        }
        assertEquals(
            1,
            simulated?.stackSize,
        )
        assertEquals(
            7,
            inventory.getStackInSlot(0).stackSize,
        )
        assertFalse(source.takeAll(
            count = 8,
        ) { stack ->
            stack.item === item
        })
        assertEquals(
            7,
            inventory.getStackInSlot(0).stackSize,
        )

        val taken: Map<ItemStack, Int> = source.take(
            count = 4,
        ) { stack ->
            stack.item === item
        }
        assertEquals(
            4,
            taken.values.sum(),
        )
        assertEquals(
            3,
            inventory.getStackInSlot(0).stackSize,
        )
        assertNull(source.takeOne { stack ->
            stack.item !== item
        })
        assertTrue(source.takeAll(
            count = 0,
        ) { stack ->
            stack.item === item
        })
    }

    @Test
    internal fun nativeSourceReceivesSimulationAndPredicateCallbacks() {
        val stack: ItemStack = ItemStack(Item())
        val simulations: MutableList<Boolean> = mutableListOf()
        var available: Int = 3
        val source: IItemSource = itemSource { predicate, simulate, count ->
            simulations.add(simulate)
            if (!predicate(stack)) {
                emptyMap()
            } else {
                val amount: Int = minOf(
                    a = count,
                    b = available,
                )
                if (!simulate) {
                    available -= amount
                }
                mapOf(stack.copy() to amount)
            }
        }

        assertTrue(source.takeAll(
            count = 2,
        ) { candidate ->
            candidate.item === stack.item
        })
        assertEquals(
            listOf(
                true,
                false,
            ),
            simulations,
        )
        assertEquals(
            1,
            available,
        )
    }
}
