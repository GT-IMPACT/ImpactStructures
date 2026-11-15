package space.impact.structures.kotlinx.animation

import java.util.function.DoubleUnaryOperator
import space.impact.structures.animation.Animations
import space.impact.structures.animation.IAnimation
import space.impact.structures.animation.RenderTransform
import space.impact.structures.animation.Vector3

public fun animation(configure: AnimationBuilder.() -> Unit): IAnimation {
    val builder = AnimationBuilder()
    configure.invoke(builder)
    return builder.build()
}

/** Custom transforms append to the supplied matrix; they do not reset operations composed before them. */
public fun animatedTransform(transform: RenderTransform.(seconds: Double) -> Unit): IAnimation {
    return IAnimation { seconds, matrix ->
        transform.invoke(
            matrix,
            seconds,
        )
    }
}

public fun spin(
    degreesPerSecond: Double,
    axis: Vector3 = Vector3.UP,
    pivot: Vector3 = Vector3.ZERO,
): IAnimation {
    return Animations.spin(
        degreesPerSecond,
        axis,
        pivot,
    )
}

public fun move(
    to: Vector3,
    durationSeconds: Double,
    from: Vector3 = Vector3.ZERO,
    easing: (Double) -> Double = Animations.SMOOTH::applyAsDouble,
): IAnimation {
    return Animations.move(
        from,
        to,
        durationSeconds,
        DoubleUnaryOperator { progress ->
            easing(progress)
        },
    )
}

public fun IAnimation.loop(durationSeconds: Double): IAnimation {
    return Animations.loop(
        this,
        durationSeconds,
    )
}

public fun IAnimation.pingPong(durationSeconds: Double): IAnimation {
    return Animations.pingPong(
        this,
        durationSeconds,
    )
}

public operator fun IAnimation.plus(other: IAnimation): IAnimation {
    return Animations.combine(
        this,
        other,
    )
}
