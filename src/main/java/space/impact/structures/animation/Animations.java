package space.impact.structures.animation;

import java.util.Objects;
import java.util.function.DoubleUnaryOperator;

/** Stateless animations can be shared between instances; the caller owns playback time. */
public final class Animations {

    public static final IAnimation STILL = (seconds, transform) -> {
    };
    public static final DoubleUnaryOperator LINEAR = value -> value;
    public static final DoubleUnaryOperator SMOOTH = value -> value * value * (3 - 2 * value);

    private Animations() {
    }

    public static IAnimation spin(double degreesPerSecond, Vector3 axis, Vector3 pivot) {
        new RenderTransform().rotate(degreesPerSecond, axis);
        Objects.requireNonNull(pivot, "pivot");
        return (seconds, transform) -> transform.rotateAround(seconds * degreesPerSecond % 360, axis, pivot);
    }

    public static IAnimation move(Vector3 from, Vector3 to, double duration, DoubleUnaryOperator easing) {
        requireDuration(duration);
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        Objects.requireNonNull(easing, "easing");
        return (seconds, transform) -> {
            double progress = easing.applyAsDouble(Math.max(0, Math.min(1, seconds / duration)));
            transform.translate(from.x + (to.x - from.x) * progress,
                    from.y + (to.y - from.y) * progress, from.z + (to.z - from.z) * progress);
        };
    }

    /** Ping-pong preserves both endpoints without a discontinuity at the loop boundary. */
    public static IAnimation pingPong(IAnimation animation, double duration) {
        requireDuration(duration);
        Objects.requireNonNull(animation, "animation");
        return (seconds, transform) -> {
            double phase = positiveModulo(seconds, duration * 2);
            animation.apply(phase <= duration ? phase : duration * 2 - phase, transform);
        };
    }

    public static IAnimation loop(IAnimation animation, double duration) {
        requireDuration(duration);
        Objects.requireNonNull(animation, "animation");
        return (seconds, transform) -> animation.apply(positiveModulo(seconds, duration), transform);
    }

    /** Applies operations in declaration order (matrix post-multiplication). */
    public static IAnimation combine(IAnimation... animations) {
        IAnimation[] copy = animations.clone();
        for (IAnimation animation : copy) {
            Objects.requireNonNull(animation, "animation");
        }
        return (seconds, transform) -> {
            for (IAnimation animation : copy) {
                animation.apply(seconds, transform);
            }
        };
    }

    private static double positiveModulo(double value, double modulus) {
        return (value % modulus + modulus) % modulus;
    }

    private static void requireDuration(double duration) {
        if (!Double.isFinite(duration) || duration <= 0 || !Double.isFinite(duration * 2)) {
            throw new IllegalArgumentException("Duration must be finite and positive");
        }
    }
}
