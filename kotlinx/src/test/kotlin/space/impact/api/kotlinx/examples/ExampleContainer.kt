package space.impact.api.kotlinx.examples

import space.impact.api.kotlinx.constructable.multiblockInfoContainer
import space.impact.api.kotlinx.constructable.registerContainer

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
