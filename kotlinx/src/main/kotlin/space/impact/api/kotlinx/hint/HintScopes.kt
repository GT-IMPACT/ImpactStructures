package space.impact.api.kotlinx.hint

import net.minecraft.world.World
import space.impact.api.ImpactAPI
import space.impact.api.kotlinx.execution.runLifecycleScope

/** Keep one hologram batch together and close it even when projection fails. */
public fun <R> withHinting(
    world: World,
    block: () -> R,
): R {
    return runLifecycleScope(
        start = {
            ImpactAPI.startHinting(world)
        },
        finish = {
            ImpactAPI.endHinting(world)
        },
        block = block,
    )
}
