package space.impact.structures.example;

import space.impact.structures.animation.Animations;

/** Deterministic, continuous timeline shared by server state and client playback. */
public final class ElevatorMotion {

    public static final double CYCLE_SECONDS = 32;
    public static final double TRAVEL = 44;

    private ElevatorMotion() {
    }

    public static double height(double seconds) {
        double phase = (seconds % CYCLE_SECONDS + CYCLE_SECONDS) % CYCLE_SECONDS;
        if (phase < 4) {
            return 0;
        }
        if (phase < 16) {
            return TRAVEL * Animations.SMOOTH.applyAsDouble((phase - 4) / 12);
        }
        if (phase < 20) {
            return TRAVEL;
        }
        return TRAVEL * (1 - Animations.SMOOTH.applyAsDouble((phase - 20) / 12));
    }

    public static long dockTick(long startTick, long now) {
        long cycleTicks = (long) (CYCLE_SECONDS * 20);
        long elapsed = Math.max(0, now - startTick);
        if (elapsed % cycleTicks < 80) {
            return now;
        }
        return startTick + (elapsed / cycleTicks + 1) * cycleTicks;
    }
}
