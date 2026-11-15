package space.impact.structures.animation;

/** Immutable vector in render-local coordinates, measured in blocks. */
public final class Vector3 {

    public static final Vector3 ZERO = new Vector3(0, 0, 0);
    public static final Vector3 UP = new Vector3(0, 1, 0);
    public final double x;
    public final double y;
    public final double z;

    public Vector3(double x, double y, double z) {
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
            throw new IllegalArgumentException("Vector components must be finite");
        }
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
