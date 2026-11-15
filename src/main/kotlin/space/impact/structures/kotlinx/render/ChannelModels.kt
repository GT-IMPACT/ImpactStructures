package space.impact.structures.kotlinx.render

import net.minecraft.world.World
import space.impact.structures.alignment.enumerable.ExtendedFacing
import space.impact.structures.render.BlockAppearance
import space.impact.structures.render.BlockPosition
import space.impact.structures.render.StructureRenderBinding
import space.impact.structures.structure.IStructureDefinition

/** Preserves the strict capture contract: unavailable selected blocks abort the complete capture. */
public fun <T> IStructureDefinition<T>.captureChannel(
    channel: String,
    world: World,
    origin: BlockPosition,
    piece: String = "main",
    facing: ExtendedFacing = ExtendedFacing.DEFAULT,
    offsetA: Int = 0,
    offsetB: Int = 0,
    offsetC: Int = 0,
    appearance: (BlockAppearance) -> BlockAppearance = { source ->
        source
    },
): StructureRenderBinding {
    return StructureRenderBinding.capture(
        this,
        piece,
        channel,
        world,
        origin,
        facing,
        BlockPosition(
            offsetA,
            offsetB,
            offsetC,
        ),
        appearance,
    )
}
