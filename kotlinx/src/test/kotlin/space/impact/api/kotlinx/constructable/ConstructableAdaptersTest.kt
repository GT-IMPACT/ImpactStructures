package space.impact.api.kotlinx.constructable

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import space.impact.api.alignment.constructable.IConstructable
import space.impact.api.alignment.constructable.IConstructableProvider
import space.impact.api.alignment.constructable.IMultiblockInfoContainer
import space.impact.api.alignment.constructable.ISurvivalConstructable
import space.impact.api.alignment.enumerable.ExtendedFacing
import space.impact.api.kotlinx.inventory.itemSource
import space.impact.api.structure.IItemSource
import space.impact.api.structure.ISurvivalBuildEnvironment

internal class ConstructableAdaptersTest {

    @Test
    internal fun receiverCallbacksHandleConstructionHintsAndNativeDescriptions() {
        val context: ContainerContext = ContainerContext()
        val trigger: ItemStack = ItemStack(Item())
        val adapter: IConstructable = constructable(
            context = context,
            description = {
                listOf("A structure")
            },
        ) { _, hintsOnly ->
            if (hintsOnly) {
                events.add("hints")
            } else {
                events.add("construct")
            }
        }
        adapter.construct(
            trigger,
            true,
        )
        adapter.construct(
            trigger,
            false,
        )

        assertEquals(
            listOf(
                "hints",
                "construct",
            ),
            context.events,
        )
        assertArrayEquals(
            arrayOf("A structure"),
            adapter.getStructureDescription(trigger),
        )
        assertNull(adapter.structureDefinition)
    }

    @Test
    internal fun survivalCallbackReceivesTheSameContextBudgetAndEnvironment() {
        val context: ContainerContext = ContainerContext()
        val trigger: ItemStack = ItemStack(Item())
        val source: IItemSource = itemSource { _, _, _ ->
            emptyMap()
        }
        val environment: ISurvivalBuildEnvironment = ISurvivalBuildEnvironment.create(
            source,
            null,
        )
        val adapter: ISurvivalConstructable = survivalConstructable(
            context = context,
            survival = { callbackTrigger, budget, callbackEnvironment ->
                assertSame(
                    trigger,
                    callbackTrigger,
                )
                assertSame(
                    environment,
                    callbackEnvironment,
                )
                events.add("budget=$budget")
                budget
            },
        ) { _, _ ->
            events.add("construct")
        }

        assertEquals(
            12,
            adapter.survivalConstruct(
                trigger,
                12,
                environment,
            ),
        )
        assertEquals(
            listOf("budget=12"),
            context.events,
        )
    }

    @Test
    internal fun providerPreservesNullabilityAndResolvesCurrentDelegate() {
        var current: IConstructable? = null
        val provider: IConstructableProvider = constructableProvider {
            current
        }
        assertNull(provider.constructable)
        val events: MutableList<String> = mutableListOf()
        current = constructable(context = events) { _, _ ->
            add("built")
        }
        assertSame(
            current,
            provider.constructable,
        )
    }

    @Test
    internal fun reifiedContainersSupportContextsThatAreNotTileEntities() {
        val trigger: ItemStack = ItemStack(Item())
        val context: ContainerContext = ContainerContext()
        assertNull(containerOrNull<UnregisteredContext>())
        val container: IMultiblockInfoContainer<ContainerContext> = multiblockInfoContainer(
            description = {
                listOf("Wrapped structure")
            },
        ) { _, hintsOnly, orientation ->
            assertTrue(hintsOnly)
            assertEquals(
                ExtendedFacing.UP_NORMAL_NONE,
                orientation,
            )
            events.add("wrapped")
        }
        registerContainer<ContainerContext>(container)
        val registered: IMultiblockInfoContainer<ContainerContext>? = containerOrNull<ContainerContext>()
        assertSame(
            container,
            registered,
        )
        val adapter: ISurvivalConstructable = requireNotNull(registered).toConstructable(
            context,
            ExtendedFacing.UP_NORMAL_NONE,
        )
        adapter.construct(
            trigger,
            true,
        )
        assertEquals(
            listOf("wrapped"),
            context.events,
        )
        assertArrayEquals(
            arrayOf("Wrapped structure"),
            adapter.getStructureDescription(trigger),
        )
    }
}
