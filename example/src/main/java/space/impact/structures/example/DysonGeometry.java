package space.impact.structures.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import space.impact.structures.animation.Animations;
import space.impact.structures.animation.IAnimation;
import space.impact.structures.animation.Vector3;
import space.impact.structures.render.BlockPosition;

/** Complete spherical cages: twelve meridians and five latitude bands, with no filled panels. */
public final class DysonGeometry {

    public static final int DIAMETER = 96;
    public static final int LAYERS = 3;
    public static final Vector3 CENTER = new Vector3(0, 49, 50);
    private static final int[] RADII = {48, 36, 24};
    private static final double[] SPEEDS = {-3, 5, -7};

    private DysonGeometry() {
    }

    public static int radius(int layer) {
        return RADII[layer];
    }

    public static IAnimation animation(int layer) {
        // Negative rotation about +Y is clockwise when seen from above.
        return Animations.spin(SPEEDS[layer], Vector3.UP, CENTER);
    }

    public static List<BlockPosition> frame(int layer) {
        int radius = radius(layer);
        List<BlockPosition> positions = new ArrayList<>();
        for (int x = -radius; x < radius; x++) {
            for (int y = -radius; y < radius; y++) {
                for (int z = -radius; z < radius; z++) {
                    if (isSurface(x, y, z, radius) && isRib(x + 0.5, y + 0.5, z + 0.5, radius)) {
                        positions.add(new BlockPosition(x, y + (int) CENTER.y, z + (int) CENTER.z));
                    }
                }
            }
        }
        return Collections.unmodifiableList(positions);
    }

    private static boolean isRib(double x, double y, double z, int radius) {
        if (Math.abs(y) < 1 || Math.abs(Math.abs(y) - radius * 0.5) < 1
                || Math.abs(Math.abs(y) - radius * Math.sqrt(3) / 2) < 1) {
            return true;
        }
        for (int meridian = 0; meridian < 6; meridian++) {
            double angle = meridian * Math.PI / 6;
            if (Math.abs(x * Math.cos(angle) + z * Math.sin(angle)) < 1) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSurface(int x, int y, int z, int radius) {
        return inside(x, y, z, radius)
                && (!inside(x - 1, y, z, radius) || !inside(x + 1, y, z, radius)
                || !inside(x, y - 1, z, radius) || !inside(x, y + 1, z, radius)
                || !inside(x, y, z - 1, radius) || !inside(x, y, z + 1, radius));
    }

    private static boolean inside(int x, int y, int z, int radius) {
        double dx = x + 0.5;
        double dy = y + 0.5;
        double dz = z + 0.5;
        return dx * dx + dy * dy + dz * dz <= radius * radius;
    }
}
