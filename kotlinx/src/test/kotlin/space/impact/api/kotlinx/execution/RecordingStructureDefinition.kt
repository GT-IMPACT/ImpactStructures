package space.impact.api.kotlinx.execution

import net.minecraft.item.ItemStack
import net.minecraft.world.World
import space.impact.api.alignment.enumerable.ExtendedFacing
import space.impact.api.structure.IStructureDefinition
import space.impact.api.structure.IStructureElement
import space.impact.api.structure.ISurvivalBuildEnvironment

internal class RecordingStructureDefinition<T> : IStructureDefinition<T> {

    internal var method: String? = null
    internal var arguments: List<Any?> = emptyList()

    override fun getStructureFor(name: String): Array<IStructureElement<T>> {
        return emptyArray()
    }

    override fun isContainedInStructure(
        name: String,
        offsetA: Int,
        offsetB: Int,
        offsetC: Int,
    ): Boolean {
        return false
    }

    override fun check(
        context: T,
        piece: String,
        world: World,
        facing: ExtendedFacing,
        worldX: Int,
        worldY: Int,
        worldZ: Int,
        offsetA: Int,
        offsetB: Int,
        offsetC: Int,
        forceCheckAllBlocks: Boolean,
    ): Boolean {
        method = "check"
        arguments = listOf(
            context,
            piece,
            world,
            facing,
            worldX,
            worldY,
            worldZ,
            offsetA,
            offsetB,
            offsetC,
            forceCheckAllBlocks,
        )
        return false
    }

    override fun hints(
        context: T,
        trigger: ItemStack?,
        piece: String,
        world: World,
        facing: ExtendedFacing,
        worldX: Int,
        worldY: Int,
        worldZ: Int,
        offsetA: Int,
        offsetB: Int,
        offsetC: Int,
    ): Boolean {
        method = "hints"
        arguments = listOf(
            context,
            trigger,
            piece,
            world,
            facing,
            worldX,
            worldY,
            worldZ,
            offsetA,
            offsetB,
            offsetC,
        )
        return true
    }

    override fun build(
        context: T,
        trigger: ItemStack?,
        piece: String,
        world: World,
        facing: ExtendedFacing,
        worldX: Int,
        worldY: Int,
        worldZ: Int,
        offsetA: Int,
        offsetB: Int,
        offsetC: Int,
    ): Boolean {
        method = "build"
        arguments = listOf(
            context,
            trigger,
            piece,
            world,
            facing,
            worldX,
            worldY,
            worldZ,
            offsetA,
            offsetB,
            offsetC,
        )
        return false
    }

    override fun survivalBuild(
        context: T,
        trigger: ItemStack?,
        piece: String,
        world: World,
        facing: ExtendedFacing,
        worldX: Int,
        worldY: Int,
        worldZ: Int,
        offsetA: Int,
        offsetB: Int,
        offsetC: Int,
        budget: Int,
        environment: ISurvivalBuildEnvironment,
        checkAfterPlacement: Boolean,
    ): Int {
        method = "survivalBuild"
        arguments = listOf(
            context,
            trigger,
            piece,
            world,
            facing,
            worldX,
            worldY,
            worldZ,
            offsetA,
            offsetB,
            offsetC,
            budget,
            environment,
            checkAfterPlacement,
        )
        return -1
    }
}
