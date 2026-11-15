package space.impact.structures.animation;

/** Playback controls independent of Minecraft, rendering and channels. Input time is world ticks + partialTicks. */
public final class AnimationClock {

    private double anchorTick;
    private double anchorSeconds;
    private double speed = 1;
    private boolean paused;

    public AnimationClock(double startTick) {
        restart(startTick);
    }

    public double secondsAt(double tick) {
        requireFinite(tick);
        return anchorSeconds + (paused ? 0 : (tick - anchorTick) * speed / 20.0);
    }

    public void restart(double tick) {
        requireFinite(tick);
        anchorTick = tick;
        anchorSeconds = 0;
        paused = false;
    }

    public void seek(double seconds, double tick) {
        requireFinite(seconds);
        requireFinite(tick);
        anchorSeconds = seconds;
        anchorTick = tick;
    }

    public void setSpeed(double speed, double tick) {
        requireFinite(speed);
        seek(secondsAt(tick), tick);
        this.speed = speed;
    }

    public void setPaused(boolean paused, double tick) {
        seek(secondsAt(tick), tick);
        this.paused = paused;
    }

    public boolean isPaused() {
        return paused;
    }

    private static void requireFinite(double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Playback values must be finite");
        }
    }
}
