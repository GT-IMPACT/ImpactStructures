package space.impact.structures.kotlinx.animation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import space.impact.structures.animation.Animations
import space.impact.structures.animation.IAnimation
import space.impact.structures.animation.RenderTransform
import space.impact.structures.animation.Vector3

internal class AnimationDslTest {

    @Test
    fun compositionPreservesOrderAndFractionalTime() {
        val motion = animation {
            move(
                to = Vector3(
                    10.0,
                    0.0,
                    0.0,
                ),
                durationSeconds = 2.0,
                easing = { progress ->
                    progress
                },
            )
            spin(degreesPerSecond = 90.0)
            custom { seconds ->
                scale(
                    1.0 + seconds,
                    1.0,
                    1.0,
                )
            }
        }
        val matrix = RenderTransform()
        motion.apply(
            0.5,
            matrix,
        )
        val point = matrix.transform(Vector3(
            1.0,
            0.0,
            0.0,
        ))
        assertEquals(
            2.5 + 1.5 / Math.sqrt(2.0),
            point.x,
            1e-9,
        )
        assertEquals(
            -1.5 / Math.sqrt(2.0),
            point.z,
            1e-9,
        )
    }

    @Test
    fun repeatAndCustomEasingPreserveJavaEndpointsAndNegativeTime() {
        val motion = move(
            to = Vector3(
                0.0,
                10.0,
                0.0,
            ),
            durationSeconds = 5.0,
        ).pingPong(durationSeconds = 5.0)
        assertEquals(
            10.0,
            sampleY(animation = motion, seconds = 5.0),
        )
        assertEquals(
            sampleY(animation = motion, seconds = -0.025),
            sampleY(animation = motion, seconds = 0.025),
            1e-9,
        )
        val repeated = motion.loop(durationSeconds = 10.0)
        assertEquals(
            sampleY(animation = repeated, seconds = 2.5),
            sampleY(animation = repeated, seconds = 12.5),
        )
        assertThrows(IllegalArgumentException::class.java) {
            motion.loop(durationSeconds = 0.0)
        }
    }

    @Test
    fun builtAnimationDoesNotObserveLaterBuilderChanges() {
        val builder = AnimationBuilder()
        builder.add(Animations.STILL)
        val snapshot = builder.build()
        builder.custom {
            translate(
                0.0,
                7.0,
                0.0,
            )
        }
        assertEquals(
            0.0,
            sampleY(animation = snapshot, seconds = 1.0),
        )
        assertEquals(
            7.0,
            sampleY(animation = snapshot + builder.build(), seconds = 1.0),
        )
    }

    @Test
    fun playbackPauseResumeSpeedAndSeekPreserveContinuity() {
        val clock = animationClock(startTick = 100.0)
        clock.pause(atTick = 120.0)
        assertEquals(
            1.0,
            clock.secondsAt(300.0),
        )
        clock.resume(atTick = 300.0)
        clock.speed(
            multiplier = -2.0,
            atTick = 320.0,
        )
        assertEquals(
            2.0,
            clock.secondsAt(320.0),
        )
        assertEquals(
            0.0,
            clock.secondsAt(340.0),
        )
        clock.seekTo(
            seconds = 10.0,
            atTick = 340.0,
        )
        assertEquals(
            9.0,
            clock.secondsAt(350.0),
        )
    }

    private fun sampleY(
        animation: IAnimation,
        seconds: Double,
    ): Double {
        val matrix = RenderTransform()
        animation.apply(
            seconds,
            matrix,
        )
        return matrix.transform(Vector3.ZERO).y
    }
}
