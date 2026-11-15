package space.impact.structures.kotlinx.execution

import net.minecraft.item.ItemStack
import net.minecraft.world.World
import space.impact.structures.alignment.enumerable.ExtendedFacing
import space.impact.structures.structure.IStructureDefinition
import space.impact.structures.structure.ISurvivalBuildEnvironment

/** Delegate to the Java traversal without changing chunk or side handling. */
public fun <T> IStructureDefinition<T>.checkStructure(
    context: T,
    world: World,
    worldX: Int,
    worldY: Int,
    worldZ: Int,
    piece: String = "main",
    facing: ExtendedFacing = ExtendedFacing.DEFAULT,
    offsetA: Int = 0,
    offsetB: Int = 0,
    offsetC: Int = 0,
    forceCheckAllBlocks: Boolean = false,
): Boolean {
    return check(
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
}

/** Trigger is required, but may be null for Java elements that do not use an item. */
public fun <T> IStructureDefinition<T>.showHints(
    context: T,
    world: World,
    worldX: Int,
    worldY: Int,
    worldZ: Int,
    trigger: ItemStack?,
    piece: String = "main",
    facing: ExtendedFacing = ExtendedFacing.DEFAULT,
    offsetA: Int = 0,
    offsetB: Int = 0,
    offsetC: Int = 0,
): Boolean {
    return hints(
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
}

public fun <T> IStructureDefinition<T>.buildCreative(
    context: T,
    world: World,
    worldX: Int,
    worldY: Int,
    worldZ: Int,
    trigger: ItemStack?,
    piece: String = "main",
    facing: ExtendedFacing = ExtendedFacing.DEFAULT,
    offsetA: Int = 0,
    offsetB: Int = 0,
    offsetC: Int = 0,
): Boolean {
    return build(
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
}

/** Return the Java built-count unchanged, including its negative completion result. */
public fun <T> IStructureDefinition<T>.buildSurvival(
    context: T,
    world: World,
    worldX: Int,
    worldY: Int,
    worldZ: Int,
    trigger: ItemStack?,
    budget: Int,
    environment: ISurvivalBuildEnvironment,
    piece: String = "main",
    facing: ExtendedFacing = ExtendedFacing.DEFAULT,
    offsetA: Int = 0,
    offsetB: Int = 0,
    offsetC: Int = 0,
    checkAfterPlacement: Boolean = false,
): Int {
    return survivalBuild(
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
}
