package space.impact.api.client;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MarkerRayTraceTest {
    @Test
    void picksClosestBlockIndependentOfDrawingOrder() {
        double far = hit(0.5, 0.5, -2, 0, 0, 1, 5, 64);
        double near = hit(0.5, 0.5, -2, 0, 0, 1, 0, 64);
        assertEquals(2, Math.min(far, near), 1e-9);
    }

    @Test
    void missesBlocksBesideOrBehindTheCrosshair() {
        assertEquals(Double.POSITIVE_INFINITY, hit(2, 0.5, -2, 0, 0, 1, 0, 64));
        assertEquals(Double.POSITIVE_INFINITY, hit(0.5, 0.5, -2, 0, 0, -1, 0, 64));
    }

    @Test
    void handlesInsideParallelAndOutOfRangeRays() {
        assertEquals(0, hit(0.5, 0.5, 0.5, 0, 1, 0, 0, 64));
        assertEquals(Double.POSITIVE_INFINITY, hit(0.5, 0.5, -2, 0, 0, 1, 0, 1.99));
        assertEquals(Double.POSITIVE_INFINITY, hit(0.5, 0.5, 0.5, 0, 0, 0, 0, 64));
        assertEquals(2, hit(0.05, 0.5, -2, 0, 0, 1, 0, 64), 1e-9);
    }

    @Test
    void picksTheWholeBlockIncludingCornersAndEdges() {
        assertEquals(2, hit(0.01, 0.99, -2, 0, 0, 1, 0, 64), 1e-9);
        assertEquals(2, hit(1, 0, -2, 0, 0, 1, 0, 64), 1e-9);
        assertEquals(Double.POSITIVE_INFINITY, hit(1.01, 0.5, -2, 0, 0, 1, 0, 64));
        assertEquals(2, hit(0.5, 0.5, -2, 0, 0, 1, 0, 2), 1e-9);
    }

    @Test
    void handlesNegativeBlockCoordinatesAndEveryViewingAxis() {
        assertEquals(2, MarkerRayTrace.blockIntersection(-2.99, -3.01, -7, 0, 0, 1, -3, -4, -5, 64), 1e-9);
        assertEquals(2, hit(-2, 0.5, 0.5, 1, 0, 0, 0, 64), 1e-9);
        assertEquals(2, hit(3, 0.5, 0.5, -1, 0, 0, 0, 64), 1e-9);
        assertEquals(2, hit(0.5, -2, 0.5, 0, 1, 0, 0, 64), 1e-9);
        assertEquals(2, hit(0.5, 3, 0.5, 0, -1, 0, 0, 64), 1e-9);
        assertEquals(2, hit(0.5, 0.5, 3, 0, 0, -1, 0, 64), 1e-9);
    }

    private static double hit(double ox, double oy, double oz, double dx, double dy, double dz, int z, double range) {
        return MarkerRayTrace.blockIntersection(ox, oy, oz, dx, dy, dz, 0, 0, z, range);
    }
}
