package space.impact.structures.kotlinx.animation

import space.impact.structures.animation.AnimationClock

public fun animationClock(
    startTick: Double,
    configure: AnimationClock.() -> Unit = {},
): AnimationClock {
    val clock = AnimationClock(startTick)
    configure.invoke(clock)
    return clock
}

public fun AnimationClock.pause(atTick: Double) {
    setPaused(
        true,
        atTick,
    )
}

public fun AnimationClock.resume(atTick: Double) {
    setPaused(
        false,
        atTick,
    )
}

public fun AnimationClock.speed(
    multiplier: Double,
    atTick: Double,
) {
    setSpeed(
        multiplier,
        atTick,
    )
}

/** Named arguments make the seconds/ticks distinction explicit at call sites. */
public fun AnimationClock.seekTo(
    seconds: Double,
    atTick: Double,
) {
    seek(
        seconds,
        atTick,
    )
}
