package space.impact.structures.kotlinx.consumer

import space.impact.structures.hint.HintColor
import space.impact.structures.kotlinx.structure.structure
import space.impact.structures.structure.IStructureDefinition

public fun consumerDefinition(): IStructureDefinition<ConsumerContext> {
    return structure<ConsumerContext> {
        shape(name = "main") {
            layer("~H")
        }

        element(
            symbol = 'H',
            element = withHint(
                element = hint(HintColor.WHITE),
                color = HintColor.BLUE,
            ),
        )
    }
}
