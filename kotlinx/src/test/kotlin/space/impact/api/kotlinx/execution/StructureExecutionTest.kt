package space.impact.api.kotlinx.execution

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import space.impact.api.alignment.enumerable.ExtendedFacing
import space.impact.api.structure.IItemSource
import space.impact.api.structure.IStructureDefinition
import space.impact.api.structure.IStructureElement
import space.impact.api.structure.ISurvivalBuildEnvironment

internal class StructureExecutionTest {

    @Test
    fun namedCheckArgumentsPreserveAxesOffsetsAndNullableContext() {
        val definition: RecordingStructureDefinition<String?> = RecordingStructureDefinition()
        val world: World = WorldToken.allocate()
        val context: String? = null

        val matches: Boolean = definition.checkStructure(
            context = context,
            world = world,
            worldX = 11,
            worldY = 22,
            worldZ = 33,
            piece = "tower",
            facing = ExtendedFacing.EAST_CLOCKWISE_HORIZONTAL,
            offsetA = 4,
            offsetB = 5,
            offsetC = 6,
            forceCheckAllBlocks = true,
        )

        assertFalse(matches)
        assertEquals(
            "check",
            definition.method,
        )
        assertEquals(
            listOf(
                context,
                "tower",
                world,
                ExtendedFacing.EAST_CLOCKWISE_HORIZONTAL,
                11,
                22,
                33,
                4,
                5,
                6,
                true,
            ),
            definition.arguments,
        )
    }

    @Test
    fun checkDefaultsUseMainPieceNativeFacingAndLoadedChunksOnly() {
        val definition: RecordingStructureDefinition<Int> = RecordingStructureDefinition()
        val world: World = WorldToken.allocate()

        definition.checkStructure(
            context = 42,
            world = world,
            worldX = 1,
            worldY = 2,
            worldZ = 3,
        )

        assertEquals(
            listOf(
                42,
                "main",
                world,
                ExtendedFacing.DEFAULT,
                1,
                2,
                3,
                0,
                0,
                0,
                false,
            ),
            definition.arguments,
        )
    }

    @Test
    fun hintAndCreativeCallsKeepRequiredNullableTrigger() {
        val definition: RecordingStructureDefinition<String> = RecordingStructureDefinition()
        val world: World = WorldToken.allocate()
        val trigger: ItemStack? = null

        assertTrue(
            definition.showHints(
                context = "hints",
                world = world,
                worldX = 1,
                worldY = 2,
                worldZ = 3,
                trigger = trigger,
            ),
        )
        assertEquals(
            "hints",
            definition.method,
        )
        assertEquals(
            listOf(
                "hints",
                trigger,
                "main",
                world,
                ExtendedFacing.DEFAULT,
                1,
                2,
                3,
                0,
                0,
                0,
            ),
            definition.arguments,
        )

        assertFalse(
            definition.buildCreative(
                context = "build",
                world = world,
                worldX = 7,
                worldY = 8,
                worldZ = 9,
                trigger = trigger,
                piece = "secondary",
                facing = ExtendedFacing.SOUTH_NORMAL_NONE,
                offsetA = 1,
                offsetB = 2,
                offsetC = 3,
            ),
        )
        assertEquals(
            "build",
            definition.method,
        )
        assertEquals(
            listOf(
                "build",
                trigger,
                "secondary",
                world,
                ExtendedFacing.SOUTH_NORMAL_NONE,
                7,
                8,
                9,
                1,
                2,
                3,
            ),
            definition.arguments,
        )
    }

    @Test
    fun survivalPassesItsEnvironmentAndKeepsNegativeCompletion() {
        val definition: RecordingStructureDefinition<String> = RecordingStructureDefinition()
        val world: World = WorldToken.allocate()
        val environment: ISurvivalBuildEnvironment = unusedEnvironment()
        val trigger: ItemStack? = null

        val built: Int = definition.buildSurvival(
            context = "survival",
            world = world,
            worldX = 11,
            worldY = 22,
            worldZ = 33,
            trigger = trigger,
            budget = 17,
            environment = environment,
            piece = "tower",
            facing = ExtendedFacing.WEST_UPSIDE_DOWN_BOTH,
            offsetA = 4,
            offsetB = 5,
            offsetC = 6,
            checkAfterPlacement = true,
        )

        assertEquals(
            -1,
            built,
        )
        assertEquals(
            listOf(
                "survival",
                trigger,
                "tower",
                world,
                ExtendedFacing.WEST_UPSIDE_DOWN_BOTH,
                11,
                22,
                33,
                4,
                5,
                6,
                17,
                environment,
                true,
            ),
            definition.arguments,
        )
    }

    @Test
    fun nativeSurvivalDoesNotTouchWorldOrEnvironmentForNonPositiveBudget() {
        val definition: IStructureDefinition<String?> = unusedDefinition()
        val world: World = WorldToken.allocate()
        val context: String? = null
        val trigger: ItemStack? = null
        for (budget in listOf(
            0,
            -1,
        )) {
            assertEquals(
                0,
                definition.buildSurvival(
                    context = context,
                    world = world,
                    worldX = 1,
                    worldY = 2,
                    worldZ = 3,
                    trigger = trigger,
                    budget = budget,
                    environment = unusedEnvironment(),
                ),
            )
        }
    }

    @Test
    fun nativeSurvivalStillRejectsAClientWorld() {
        val definition: IStructureDefinition<String?> = unusedDefinition()
        val world: World = WorldToken.allocate()
        world.isRemote = true
        val context: String? = null
        val trigger: ItemStack? = null

        assertThrows(
            IllegalArgumentException::class.java,
            {
                definition.buildSurvival(
                    context = context,
                    world = world,
                    worldX = 1,
                    worldY = 2,
                    worldZ = 3,
                    trigger = trigger,
                    budget = 1,
                    environment = unusedEnvironment(),
                )
            },
        )
    }

    private fun unusedEnvironment(): ISurvivalBuildEnvironment {
        return object : ISurvivalBuildEnvironment {

            override fun getSource(): IItemSource {
                throw AssertionError("The environment must not be consulted")
            }

            override fun getActor(): EntityPlayer {
                throw AssertionError("The actor must not be consulted")
            }
        }
    }

    private fun unusedDefinition(): IStructureDefinition<String?> {
        return object : IStructureDefinition<String?> {

            override fun getStructureFor(name: String): Array<IStructureElement<String?>> {
                throw AssertionError("The structure must not be consulted")
            }

            override fun isContainedInStructure(
                name: String,
                offsetA: Int,
                offsetB: Int,
                offsetC: Int,
            ): Boolean {
                throw AssertionError("The structure must not be consulted")
            }
        }
    }
}
