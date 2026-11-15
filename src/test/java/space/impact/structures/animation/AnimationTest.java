package space.impact.structures.animation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class AnimationTest {

    @Test
    void playbackPauseResumeSpeedAndSeekDoNotJump() {
        AnimationClock clock = new AnimationClock(1000);
        assertEquals(2.025, clock.secondsAt(1040.5), 1e-10);
        clock.setPaused(true, 1040);
        assertEquals(2, clock.secondsAt(2000));
        clock.setPaused(false, 2000);
        clock.setSpeed(2, 2020);
        assertEquals(3, clock.secondsAt(2020));
        assertEquals(5, clock.secondsAt(2040));
        clock.seek(8, 2040);
        assertEquals(8, clock.secondsAt(2040));
        clock.setSpeed(-1, 2040);
        assertEquals(7, clock.secondsAt(2060));
    }

    @Test
    void rotationPreservesPivotAndMovesAGroupAsOneBody() {
        Vector3 pivot = new Vector3(4.5, 7.5, -3.5);
        RenderTransform transform = new RenderTransform().rotateAround(90, Vector3.UP, pivot);
        assertPoint(pivot, transform.transform(pivot));
        assertPoint(new Vector3(4.5, 7.5, -5.5), transform.transform(new Vector3(6.5, 7.5, -3.5)));
    }

    @Test
    void fractionalTimeIsNotQuantizedToBlocksOrTicks() {
        IAnimation movement = Animations.move(Vector3.ZERO, new Vector3(0, 10, 0), 5, Animations.LINEAR);
        RenderTransform transform = new RenderTransform();
        movement.apply(0.0125, transform);
        assertEquals(0.025, transform.transform(Vector3.ZERO).y, 1e-10);
    }

    @Test
    void smoothPingPongIsContinuousAtBothTurnsAndWrapsNegativeTime() {
        IAnimation movement = Animations.pingPong(
                Animations.move(Vector3.ZERO, new Vector3(0, 10, 0), 5, Animations.SMOOTH), 5);
        assertEquals(10, sampleY(movement, 5), 1e-10);
        assertEquals(0, sampleY(movement, 10), 1e-10);
        assertEquals(sampleY(movement, 5 - 0.001), sampleY(movement, 5 + 0.001), 1e-10);
        assertEquals(sampleY(movement, -0.001), sampleY(movement, 0.001), 1e-10);
    }

    @Test
    void operationOrderAndScaleArePredictable() {
        RenderTransform transform = new RenderTransform().translate(10, 0, 0)
                .rotate(90, Vector3.UP).scale(2, 3, 4);
        assertPoint(new Vector3(10, 0, -2), transform.transform(new Vector3(1, 0, 0)));
        assertPoint(new Vector3(1, 2, 3), transform.identity().transform(new Vector3(1, 2, 3)));
    }

    @Test
    void invalidTimelinesAndAxesFailBeforeRendering() {
        assertThrows(IllegalArgumentException.class, () -> Animations.loop(Animations.STILL, 0));
        assertThrows(IllegalArgumentException.class, () -> Animations.pingPong(Animations.STILL, Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> Animations.spin(30, Vector3.ZERO, Vector3.ZERO));
    }

    private static double sampleY(IAnimation animation, double seconds) {
        RenderTransform transform = new RenderTransform();
        animation.apply(seconds, transform);
        return transform.transform(Vector3.ZERO).y;
    }

    private static void assertPoint(Vector3 expected, Vector3 actual) {
        assertEquals(expected.x, actual.x, 1e-9);
        assertEquals(expected.y, actual.y, 1e-9);
        assertEquals(expected.z, actual.z, 1e-9);
    }
}
