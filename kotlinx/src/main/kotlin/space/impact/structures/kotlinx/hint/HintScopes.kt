package space.impact.structures.kotlinx.hint

import net.minecraft.world.World
import space.impact.structures.ImpactStructures
import space.impact.structures.kotlinx.execution.runLifecycleScope

/** Keep one hologram batch together and close it even when projection fails. */
public fun <R> withHinting(
    world: World,
    block: () -> R,
): R {
    return runLifecycleScope(
        start = {
            ImpactStructures.startHinting(world)
        },
        finish = {
            ImpactStructures.endHinting(world)
        },
        block = block,
    )
}
