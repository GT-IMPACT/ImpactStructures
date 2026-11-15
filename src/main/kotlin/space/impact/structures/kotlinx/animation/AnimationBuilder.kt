package space.impact.structures.kotlinx.animation

import space.impact.structures.animation.Animations
import space.impact.structures.animation.IAnimation
import space.impact.structures.animation.RenderTransform
import space.impact.structures.animation.Vector3

/** Combines operations in declaration order, preserving Java's matrix post-multiplication. */
@AnimationDsl
public class AnimationBuilder {

    private val operations: MutableList<IAnimation> = mutableListOf()

    public fun add(animation: IAnimation) {
        operations.add(animation)
    }

    public fun spin(
        degreesPerSecond: Double,
        axis: Vector3 = Vector3.UP,
        pivot: Vector3 = Vector3.ZERO,
    ) {
        add(space.impact.structures.kotlinx.animation.spin(
            degreesPerSecond = degreesPerSecond,
            axis = axis,
            pivot = pivot,
        ))
    }

    public fun move(
        to: Vector3,
        durationSeconds: Double,
        from: Vector3 = Vector3.ZERO,
        easing: (Double) -> Double = Animations.SMOOTH::applyAsDouble,
    ) {
        add(space.impact.structures.kotlinx.animation.move(
            to = to,
            durationSeconds = durationSeconds,
            from = from,
            easing = easing,
        ))
    }

    public fun custom(transform: RenderTransform.(seconds: Double) -> Unit) {
        add(animatedTransform(transform))
    }

    public fun build(): IAnimation {
        return Animations.combine(*operations.toTypedArray())
    }
}
