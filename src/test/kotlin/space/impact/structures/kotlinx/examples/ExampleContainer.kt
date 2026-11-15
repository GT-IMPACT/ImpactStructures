package space.impact.structures.kotlinx.examples

import space.impact.structures.kotlinx.constructable.multiblockInfoContainer
import space.impact.structures.kotlinx.constructable.registerContainer

/** Optional adapter pattern for a foreign controller that cannot implement the constructable interfaces itself. */
public fun registerExampleContainer() {
    registerContainer<ExampleController>(
        container = multiblockInfoContainer<ExampleController>(
            description = { trigger ->
                listOf(
                    "External controller adapter",
                    "Selected master value: ${trigger.stackSize}",
                )
            },
            survival = { trigger, budget, environment, facing ->
                survivalAtFacing(
                    trigger = trigger,
                    budget = budget,
                    environment = environment,
                    facing = facing,
                )
            },
            construct = { trigger, hintsOnly, facing ->
                constructAtFacing(
                    trigger = trigger,
                    hintsOnly = hintsOnly,
                    facing = facing,
                )
            },
        ),
    )
}
