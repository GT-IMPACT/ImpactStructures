package space.impact.structures.animation;

/** Appends a pose to the supplied transform. Time is continuous seconds, including partial ticks. */
@FunctionalInterface
public interface IAnimation {

    void apply(double seconds, RenderTransform transform);
}
