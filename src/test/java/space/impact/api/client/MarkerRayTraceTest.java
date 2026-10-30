package space.impact.api.client;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MarkerRayTraceTest {
    @Test
    void picksClosestFrameIndependentOfDrawingOrder() {
        double far = hit(0.5, 0.5, -2, 0, 0, 1, 5, 64);
        double near = hit(0.5, 0.5, -2, 0, 0, 1, 0, 64);
        assertEquals(2.05, Math.min(far, near), 1e-9);
    }

    @Test
    void missesFramesBesideOrBehindTheCrosshair() {
        assertEquals(Double.POSITIVE_INFINITY, hit(2, 0.5, -2, 0, 0, 1, 0, 64));
        assertEquals(Double.POSITIVE_INFINITY, hit(0.5, 0.5, -2, 0, 0, -1, 0, 64));
    }

    @Test
    void handlesInsideParallelAndOutOfRangeRays() {
        assertEquals(0, hit(0.5, 0.5, 0.5, 0, 1, 0, 0, 64));
        assertEquals(Double.POSITIVE_INFINITY, hit(0.5, 0.5, -2, 0, 0, 1, 0, 2));
        assertEquals(Double.POSITIVE_INFINITY, hit(0.5, 0.5, 0.5, 0, 0, 0, 0, 64));
        assertEquals(2.05, hit(0.05, 0.5, -2, 0, 0, 1, 0, 64), 1e-9);
    }

    private static double hit(double ox, double oy, double oz, double dx, double dy, double dz, int z, double range) {
        return MarkerRayTrace.intersection(ox, oy, oz, dx, dy, dz, 0.05, 0.05, z + 0.05, 0.95, 0.95, z + 0.95, range);
    }
}
