package space.impact.api.kotlinx.consumer

import space.impact.api.hint.HintColor
import space.impact.api.kotlinx.structure.structure
import space.impact.api.structure.IStructureDefinition

public fun consumerDefinition(): IStructureDefinition<ConsumerContext> {
    return structure<ConsumerContext> {
        shape(name = "main") {
            layer("~H")
        }

        element(
            symbol = 'H',
            element = hint(HintColor.WHITE),
        )
    }
}
