package space.impact.api.client;

import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import space.impact.api.hint.HintMarkerKind;

/** Ray picking and world obstruction checks used by hologram labels. */
public final class MarkerRayTrace {
    private MarkerRayTrace() {}

    public static double obstructionDistance(World world, Vec3 from, Vec3 to) {
        // World ray tracing mutates 'from' as it traverses cells. Keep the original eye coordinates.
        double eyeX = from.xCoord;
        double eyeY = from.yCoord;
        double eyeZ = from.zCoord;
        MovingObjectPosition obstruction = world.func_147447_a(from, to, false, true, false);
        return obstruction == null ? Double.POSITIVE_INFINITY
                : Math.sqrt(obstruction.hitVec.squareDistanceTo(eyeX, eyeY, eyeZ));
    }

    public static double hintIntersection(double ox, double oy, double oz, double dx, double dy, double dz,
            int x, int y, int z, HintMarkerKind kind, double range) {
        return hintIntersection(ox, oy, oz, dx, dy, dz, x, y, z, kind, false, range);
    }

    public static double hintIntersection(double ox, double oy, double oz, double dx, double dy, double dz,
            int x, int y, int z, HintMarkerKind kind, boolean colorFrame, double range) {
        double size = HintRenderStyle.displaySize(kind, colorFrame);
        double inset = (HintRenderStyle.BLOCK_SIZE - size) / 2;
        return intersection(ox, oy, oz, dx, dy, dz, x + inset, y + inset, z + inset,
                x + inset + size, y + inset + size, z + inset + size, range);
    }

    public static double blockIntersection(double ox, double oy, double oz, double dx, double dy, double dz,
            int x, int y, int z, double range) {
        return intersection(ox, oy, oz, dx, dy, dz, x, y, z,
                x + HintRenderStyle.BLOCK_SIZE, y + HintRenderStyle.BLOCK_SIZE, z + HintRenderStyle.BLOCK_SIZE, range);
    }

    /** Return forward ray distance to a box, or positive infinity when it is not hit within range. */
    public static double intersection(double ox, double oy, double oz, double dx, double dy, double dz,
            double minX, double minY, double minZ, double maxX, double maxY, double maxZ, double range) {
        if (range < 0 || dx * dx + dy * dy + dz * dz < 1e-24) return Double.POSITIVE_INFINITY;
        double near = 0, far = range;
        if (Math.abs(dx) < 1e-12) {
            if (ox < minX || ox > maxX) return Double.POSITIVE_INFINITY;
        } else {
            double a = (minX - ox) / dx, b = (maxX - ox) / dx;
            near = Math.max(near, Math.min(a, b));
            far = Math.min(far, Math.max(a, b));
        }
        if (Math.abs(dy) < 1e-12) {
            if (oy < minY || oy > maxY) return Double.POSITIVE_INFINITY;
        } else {
            double a = (minY - oy) / dy, b = (maxY - oy) / dy;
            near = Math.max(near, Math.min(a, b));
            far = Math.min(far, Math.max(a, b));
        }
        if (Math.abs(dz) < 1e-12) {
            if (oz < minZ || oz > maxZ) return Double.POSITIVE_INFINITY;
        } else {
            double a = (minZ - oz) / dz, b = (maxZ - oz) / dz;
            near = Math.max(near, Math.min(a, b));
            far = Math.min(far, Math.max(a, b));
        }
        return near <= far ? near : Double.POSITIVE_INFINITY;
    }
}
