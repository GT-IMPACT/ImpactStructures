package space.impact.structures.kotlinx.consumer

import space.impact.structures.hint.HintColor
import space.impact.structures.kotlinx.structure.structure
import space.impact.structures.structure.IStructureDefinition
import space.impact.structures.animation.IAnimation
import space.impact.structures.animation.Vector3
import space.impact.structures.kotlinx.animation.animation
import space.impact.structures.kotlinx.animation.pingPong

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

public fun consumerAnimation(): IAnimation {
    return animation {
        move(
            to = Vector3(
                0.0,
                12.0,
                0.0,
            ),
            durationSeconds = 4.0,
        )
        spin(degreesPerSecond = 30.0)
    }.pingPong(durationSeconds = 4.0)
}
