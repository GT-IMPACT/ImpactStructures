package space.impact.structures.kotlinx.render.client

import net.minecraft.world.World
import space.impact.structures.render.BlockPosition
import space.impact.structures.render.RenderPart
import space.impact.structures.render.client.RenderHandle
import space.impact.structures.render.client.StructureRenderer

/** Call from the client main thread; retain the handle and close it with the owning controller. */
public fun World.showStructureRender(
    origin: BlockPosition,
    startTick: Double,
    vararg parts: RenderPart,
): RenderHandle {
    return StructureRenderer.INSTANCE.show(
        this,
        origin,
        startTick,
        *parts,
    )
}
